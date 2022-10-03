(ns builder
  (:require [bask.bask :refer [ask!]]
            [clojure.term.colors :as c]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def basic-qqs [{:id :id
                 :msg "What key should this go to? (blank for none)"
                 :fn/out #(if (= "" %) nil (keyword %))}
                {:id :msg
                 :msg "Want it to say something? (blank for none)"
                 :fn/out #(get {"" nil} % %)}])

(def select-qqs [{:id :choices
                  :type :text
                  :msg "Enter the choices as a comma-delimited list"
                  :fn/out #(str/split % #",")}])

(def concatv (comp vec concat))

(defn question-type->fields
  [question-type]
  (case question-type
    :text basic-qqs
    :number basic-qqs
    :bool  basic-qqs
    :select (concatv basic-qqs select-qqs)
    :multi  (concatv basic-qqs select-qqs)))

(let [*continue? (atom true)
      *questions (atom [])]
  (while @*continue?
    (let [question-type (ask! {:type :select
                               :msg (str "What kind of question do you want for #" (inc (count @*questions)) "?")
                               :choices [:text :number :bool :select :multi]
                               :fn/out edn/read-string})
          qs (question-type->fields question-type)
          _ (pp/pprint ["QS:" qs])
          answer (assoc (ask! qs) :type question-type)]
      ;; (pp/pprint ["Answer:" answer])
      (swap! *questions conj answer)
      (reset! *continue?
              (ask! {:type :bool :initial false :msg "Make another question?"}))))

  (println (c/cyan "You can use these questions like so: \n"))

  (str "(ask! " (pr-str @*questions) ")"))
