(ns ask
  (:require [bask.bask :refer [ask!]]
            [clojure.edn :as edn]))

(ask! (edn/read-string (first *command-line-args*)))
