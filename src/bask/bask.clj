#!/usr/bin/env bb

(ns bask.bask
  (:require
   [babashka.tasks :refer [shell]]
   [selmer.parser :refer [<<]]
   [babashka.fs :as fs]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.term.colors :as c]))

(defn- install-or-noop [program install-fn]
  (letfn [(can-run? [program] (= 0 (:exit (shell {:out nil} (str "command -v " program)))))]
    (when-not (can-run? program)
      (println (<< "You don't have {{program}} installed. Attempting to install now..."))
      (install-fn)
      (println (<< "{{program}} should be installed now. Thanks!")))))

(install-or-noop "fzf" (fn [] (shell "brew install fzf")))

(defn print-prompt! [message]
  (print (str (c/green " ? ") (c/white message) ": ")) (flush))

(defn -prompt
  [message]
  (print-prompt! message)
  (let [result (read-line)]
    (if result (str/trim result) "")))

(defmulti ask!* (fn [x] (:type x)))

(defn ->title [question]
  (c/white (or (:msg question)
               (when (:id question) (str/capitalize (name (:id question))))
               "")))

(defmethod ask!* :default
  [question]
  (ask!* (-> question
             (assoc :type :text)
             (assoc :msg (->title question)))))

(defmethod ask!* :text
  [{:keys [id] :as question}]
  (let [result (-prompt (->title question))]
    (if id {id result} result)))

(defn- number-prompt [question]
  (try (let [n (edn/read-string (-prompt (->title question)))]
         (if (number? n)
           n
           (throw (ex-info "needs a number" {}))))
       (catch Exception _
         (println (c/cyan "Please enter a number"))
         (number-prompt question))))

(defmethod ask!* :number
  [{:keys [id] :as question}]
  (let [result (number-prompt question)]
    (if id {id result} result)))

(defmethod ask!* :select
  [{:keys [id choices initial] :as question}]
  (println "Question: " question)
  (let [result (->> @(shell {:in (str/join "\n" choices) :out :string}
                            (str "fzf "
                                 "--height 10 "
                                 "--layout reverse "
                                 (when initial (str "--query=\"" initial "\" "))
                                 "--prompt=\"? " (->title question) ": \""))
                    :out
                    str/trim)]
    (print-prompt! (->title question)) (println result)
    (if id {id result} result)))

(defmethod ask!* :multi
  [{:keys [id choices initial] :as question}]
  (let [result (->> @(shell {:in (str/join "\n" choices) :out :string}
                            (str "fzf "
                                 "--multi "
                                 "--height 10 "
                                 "--layout reverse "
                                 (when initial (str "--query=\"" initial "\" "))
                                 "--prompt=\"? " (->title question) ": \""))
                    :out
                    str/split-lines
                    (mapv str/trim))]
    (print-prompt! (->title question)) (println result)
    (if id {id result} result)))

(defn- mappify-q
  "Questions can be nil, a string, or a map. This converts them all to maps."
  [q]
  (cond
    (string? q) {:msg q}
    (nil? q)    {:msg ""}
    (map? q)    q))

(defn prepare-questions
  "This is called only when asking mulitle questions, will fill in the question's index as its :id, if none is given"
  [q-maps]
  (map-indexed
    (fn [i q-map]
      (if-let [[_ id-key] (find q-map :id)]
        q-map
        (do
          (println "warning: adding implicit id" i "to question" (str (apply str (take 100 (pr-str q-map))) ".")
                   "\nDid you mean to give it an :id?")
          (assoc q-map :id i))))
    q-maps))

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

  (ask! {:id :a} {:id b}) ;; => get a map {:a \"one\" :b \"two\"}
  (ask! nil nil) ;; => get a map {0 \"a\" 1 \"b\"}
  (ask! {:id :a} nil) ;; => get a map {:a \"one\" 1 \"two\"}
  (ask! {:id :host :type :text}
        {:id :port :type :number}) ;; => get a map {:host \"localhost\" :port 2399}
  "

  ([] (ask!* nil))
  ([q] (ask!* (mappify-q q)))
  ([& qs]
   (let [prepared-questions (prepare-questions (map mappify-q qs))]
     (into {} (map ask!* prepared-questions)))))

(comment

  ;; types:
  ;; <none> - defaults to text
  ;; :text - prompts for a string
  ;; :number - like text, but must be parseable, or asks again
  ;; :select - autocomplete / narrowing for selecting 1 item
  ;; :multi - autocomplete / narrowing for selecting 0, 1, or many items returned as a vector

  )
