#!/usr/bin/env bb

(ns bask.bask
  (:require
   [babashka.tasks :refer [shell]]
   [selmer.parser :refer [<<]]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [bask.colors :as c]))

(defn- install-or-noop [program install-fn]
  (letfn [(can-run? [program] (= 0 (:exit (shell {:out nil} (str "command -v " program)))))]
    (when-not (can-run? program)
      (println (<< "You don't have {{program}} installed. Attempting to install now..."))
      (install-fn)
      (println (<< "{{program}} should be installed now. Thanks!")))))

(install-or-noop "fzf" (fn [] (shell "brew install fzf")))

(defn- print-prompt! [message]
  (print (str (c/green " ? ") message ": ")) (flush))

(defn- -prompt
  [message]
  (print-prompt! message)
  (let [result (read-line)]
    (if result (str/trim result) "")))

;; Types:
;; <none> - defaults to text
;; :text - prompts for a string
;; :number - like text, but must be parseable, or asks again
;; :select - autocomplete / narrowing for selecting 1 item
;; :multi - autocomplete / narrowing for selecting 0, 1, or many items returned as a vector
;; :bool - select true or false

(defmulti ask!* (fn [x] (:type x)))

(defn ->title [question]
  (try (str (or (:msg question)
                (when (:id question) (str/capitalize (name (:id question))))
                (case (:type question)
                  (:number "number") "#  "
                  (:text "text") "   "
                  (:bool "bool") "t/f"
                  nil))
            (when (:init question)
              (str " (default '" (:init question) "')")))
       (catch Exception _ "")))

(defn ->out [{:keys [id] :fn/keys [out] :as q} result]
  (let [parsed-result (cond-> result out out)]
    (if id {id parsed-result} parsed-result)))

(defmethod ask!* :default
  [question]
  (ask!* (-> question
             (assoc :type :text)
             (assoc :msg (->title question)))))

(defn- fill-init [init result]
  (if (and init (str/blank? result)) init result))

(defmethod ask!* :text
  [{:keys [init] :as question}]
  (let [result (->> question ->title -prompt)
        result-after-init-applied (if (and init (str/blank? result)) init result)]
    (->out question result-after-init-applied)))

(defn ->number [s]
  (or (parse-long s) (parse-double s)))

(defn- number-prompt [question]
  (try (let [prompt-result (-prompt (->title question))
             n (->number prompt-result)
             niq (if (string? (:init question)) (->number (:init question)) (:init question))]
         (cond
           (and (:init question) (number? niq) (str/blank? prompt-result))
           niq

           (number? n)
           n

           :else
           (throw (ex-info "needs a number, or :init" {}))))
       (catch Exception _
         (println (c/cyan "Please enter a number"))
         (number-prompt question))))

(defmethod ask!* :number
  [question]
  (->> question number-prompt (->out question)))

(defn- pull-to-front
  [coll v]
  (sort-by #(not= % v) coll))

(defmethod ask!* :select
  [{:keys [choices init] :as question}]
  ;;(println "Question: " question)
  (let [result (->> @(shell {:in (str/join "\n" (if init
                                                  (pull-to-front choices init)
                                                  choices))
                             :out :string}
                            (str "fzf "
                                 "--height 10 "
                                 "--layout reverse "
                                 "--prompt=\"? " (->title question) ": \" "
                                 "--no-mouse"))
                    :out
                    str/trim)]
    (print-prompt! (->title question)) (println result)
    (->out question result)))

(defmethod ask!* :multi
  [{:keys [choices init] :as question}]
  (println (c/cyan "Use TAB or Shift-TAB to select choice(s)"))
  (let [result (->> @(shell {:in (str/join "\n"
                                           (if init
                                             (pull-to-front choices init)
                                             choices)) :out :string}
                            (str "fzf "
                                 "--multi "
                                 "--height 10 "
                                 (when init (str "--query=\"" init "\" "))
                                 "--layout reverse "
                                 "--prompt=\"? " (->title question) ": \" "
                                 "--no-mouse"))
                    :out
                    str/split-lines
                    (mapv str/trim))]
    (print-prompt! (->title question)) (println result)
    (->out question result)))

(defmethod ask!* :bool
  [{:keys [init] :as question}]
  (let [result (->> @(shell {:in (if (#{"true" true nil} init) ;; true first
                                   "true\nfalse"
                                   "false\ntrue")
                             :out :string}
                            (str "fzf "
                                 "--height 10 "
                                 "--layout reverse "
                                 (when init (str "--query=\"" init "\" "))
                                 "--prompt=\"? " (->title question) ": \" "
                                 "--no-mouse"))
                    :out
                    str/trim
                    (= "true"))]
    (print-prompt! (->title question)) (println result)
    (->out question result)))

(defn- mappify-q
  "Questions can be nil, a string, or a map. This converts them all to maps."
  [q]
  (cond
    (string? q) {:msg q}
    (nil? q)    {:msg ""}
    (map? q)    q))

(defn- add-index-ids
  "fill in the question's index as its :id, if none is given"
  [q-maps]
  (map-indexed
   (fn [i q-map]
     (if-let [[_ id-key] (find q-map :id)]
       q-map
       (do
         #_(println "warning: adding implicit id" i "to question" (str (apply str (take 100 (pr-str q-map))) ".")
                    "\nDid you mean to give it an :id?")
         (assoc q-map :id i))))
   q-maps))

(defn- ensure-vec [maybe-sequential]
  (if (sequential? maybe-sequential) maybe-sequential [maybe-sequential]))

(defn ask!
  "Ask questions.

  single question examples:

  - if there is no :id, return the value itself
  - if there is an :id, return a map of {:id value}

  (ask! nil) ;; => get a string \"input\"
  (ask! \"my q\") ;; => get a string \"input\"
  (ask! {:id :name}) ;; => auto-string type {:name \"input\"}
  (ask! {:id :first-name :type :text}) ;; {:first-name \"input\"}
  (ask! {:id :age :msg \"current age\" :type :number}) ;; => {:age 29}
  (ask! {:id :word :type :select :choices (vec (shuffle (str/split-lines (slurp \"/usr/share/dict/web2\"))))}) ;; => {:word \"apple\"}
  (ask! {:id :words :type :multi :choices (vec (shuffle (str/split-lines (slurp \"/usr/share/dict/web2\"))))}) ;; => {:words [\"apple\"]}

  multiple quesiton examples:

  - when missing an :id, the question's id will be located at it's index.

  (ask! [{:id :a} {:id b}]) ;; => get a map {:a \"one\" :b \"two\"}
  (ask! [nil nil]) ;; => get a map {0 \"a\" 1 \"b\"}
  (ask! [{:id :a} nil]) ;; => get a map {:a \"one\" 1 \"two\"}
  (ask! [{:id :host :type :text}
         {:id :port :type :number}]) ;; => get a map {:host \"localhost\" :port 2399}


  - for inputs that select a value, you can set an :init value that will be highlighted first.
  "
  ([] (ask!* nil))
  ([maybe-vec-qs]
   (let [qs (ensure-vec maybe-vec-qs)
         prepared-questions (add-index-ids (map mappify-q qs))
         results (mapv ask!* prepared-questions)]
     ;; Return the value, when there is one question and it did NOT have an :id
     (if (and (= 1 (count qs)) (not (contains? (first qs) :id)))
       (get (first results) 0)
       (into {} results)))))

