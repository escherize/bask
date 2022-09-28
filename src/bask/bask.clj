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
      (println (<< "You don't have {{program}} installed. Installing now..."))
      (install-fn)
      (println (<< "{{program}} should be installed now. Thanks!")))))

(install-or-noop "fzf" (fn [] (shell "brew install fzf")))

(defn print-prompt! [message]
  (print (str (c/green " ? ") (c/white message) ": ")) (flush))

(defn -prompt
  [message]
  (print-prompt! message)
  (str/trim (read-line)))

(defmulti ask!* (fn [x] (:type x)))

(defn ->title [question]
  (c/white (or (:msg question) (str/capitalize (name (:id question))) "")))

(defmethod ask!* :default
  [question]
  (cond
    (string? question) (ask!* {:msg question})
    (nil? question) (-prompt "")
    (map? question) (ask!* (-> question
                               (assoc :type :text)
                               (assoc :msg (->title question))))))

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
  (let [result (->> @(shell {:in (str/join "\n" choices) :out :string}
                            (str "fzf "
                                 "--height 10 "
                                 "--layout reverse "
                                 (when initial (str "--query=\"" initial "\""))
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
                                 (when initial (str "--query=\"" initial "\""))
                                 "--prompt=\"? " (->title question) ": \""))
                    :out
                    str/split-lines
                    (mapv str/trim))]
    (print-prompt! (->title question)) (println result)
    (if id {id result} result)))

(defn ask!
  ([] (ask!* nil))
  ([v] (ask!* v))
  ([& vs] (into {} (mapv ask! (flatten vs)))))

nil ;; => get a string "input"
"my q" ;; => get a string "input"
{:id :name} ;; => auto-string type {:name "input"}
{:id :first-name :type :text} ;; {:first-name "input"}
{:id :age :msg "current age" :type :number} ;; => {:age 29}
{:id :word :type :select :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))} ;; => {:word "apple"}
{:id :words :type :multi :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))} ;; => {:words ["apple"]}


;; types:
;; <none> - defaults to text
;; :text - prompts for a string
;; :number - like text, but must be parseable, or asks again
;; :bool - prompts for true or false
;; :select - autocomplete / narrowing for selecting 1 item
;; :multi - autocomplete / narrowing for selecting 0, 1, or many items returned as a vector


;; (println (pr-str (ask!)))
;; (println (pr-str (ask! "just a string")))
;; (println (pr-str (ask! {:id :apple})))

(ask! #_#_{:id :name}
      {:id :age :type :number :msg "Current age?"}
      {:id :color
       :type :select
       :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))}
      {:id :multi-color
       :type :multi
       :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))})
