(ns bask.bask_test
  (:require
   [clojure.test :refer [deftest is]]
   [babashka.tasks :refer [shell]]))

(defn prompt+
  [value]
  (str "[32m ? [0m[37m[37m[37m[0m[0m[0m: \"" value "\"\n"))

(defn word-seq []
  (repeatedly #(apply str (repeatedly
                            (rand-int 20)
                            (fn [] (rand-nth "qwertyuiopasdfghjklzxcvbnm"))))))

(deftest ask!-test
  (doseq [w (take 100 (word-seq))]
    (is (= (prompt+ w)
           (:out @(shell {:in w :out :string} "bb examples/simple.clj"))))))

(deftest ask!-multiples-test
  (println
    (let [w (str (first (word-seq)) "\n" (first (word-seq)))]
      (:out @(shell {:in w :out :string} "bb examples/two.clj")))))
