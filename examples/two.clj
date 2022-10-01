(ns two
  (:require [bask.bask :refer [ask!]]))

(ask! [{:id :name} ;; implicitly :type :text
       {:id :age :type :number}
       {:id :fav-color
        :type :select
        :choices ["red" "green" "blue" "white"
                  "orange" "yellow" "gray" "grey"]}
       {:id :pizza-toppings
        :type :multi
        :choices ["arugula" "chicken" "onions" "basil"
                  "raw tomatoes" "garlic" "pepperoni"]}])
