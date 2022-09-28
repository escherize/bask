(ns bask.bask_test
  (:require
   [clojure.test :refer [deftest is]]
   [babashka.tasks :refer [shell]]
   [clojure.string :as str]))

(defn prompt+
  [value]
  (str "[32m ? [0m[37m[37m[37m[0m[0m[0m: \"" value "\"\n"))

(defn word-seq []
  (repeatedly #(apply str (repeatedly
                            (rand-int 20)
                            (fn [] (rand-nth "qwertyuiopasdfghjklzxcvbnm"))))))

(defn word [] (first (word-seq)))

(deftest ask!-test
  (doseq [w (take 100 (word-seq))]
    (is (= (prompt+ w)
           (:out @(shell {:in w :out :string} "bb examples/simple.clj"))))))

(deftest ask!-multiples-test
  (let [w1 (word) w2 (word) w (str w1 "\n" w2)]
    (is (str/ends-with? (last (str/split-lines (:out @(shell {:in w :out :string} "bb examples/two.clj"))))
                        (pr-str {0 w1 1 w2})))))
