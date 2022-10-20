(ns init
  (:require [bask.bask :refer [ask!]]
            [bask.colors :as c]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]
            [clojure.string :as str]))


(println "Hit enter a bunch of times to fill in the defaults.\n")

(def defaults (ask!
                [{:id :a :type :text :init "hi"}
                 {:id :b :type :number :init 42}
                 {:id :c :type :bool :init true}
                 {:id :d :type :select :init "x" :choices ["a" "b" "x" "g"]}
                 {:id :e :type :multi :init "c" :choices ["a" "b" "c"]}]))


(println "\nThe defaults should be:"
         (pp/pprint {:a "hi", :b 42, :c true, :d "x", :e ["c"]})
         "|"
         (= defaults {:a "hi", :b 42, :c true, :d "x", :e ["c"]}))
