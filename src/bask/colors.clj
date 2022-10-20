(ns bask.colors
  (:require [clojure.string :as str]))

(defn- escape [i] (str "\033[" i "m"))
(def ^:dynamic *disable-colors* false)
(def reset (escape 0))

(defn- apply-color [color-code args]
  (if *disable-colors*
    (apply str args)
    (str
      (str/join (map #(str color-code %) args))
      reset)))

(defn bold          [& args] (apply-color "[1m" args))
(defn dark          [& args] (apply-color "[2m" args))
(defn underline     [& args] (apply-color "[4m" args))
(defn blink         [& args] (apply-color "[5m" args))
(defn reverse-color [& args] (apply-color "[7m" args))
(defn concealed     [& args] (apply-color "[8m" args))

(defn gray          [& args] (apply-color "[30m" args))
(defn grey          [& args] (apply-color "[30m" args))
(defn red           [& args] (apply-color "[31m" args))
(defn green         [& args] (apply-color "[32m" args))
(defn yellow        [& args] (apply-color "[33m" args))
(defn blue          [& args] (apply-color "[34m" args))
(defn magenta       [& args] (apply-color "[35m" args))
(defn cyan          [& args] (apply-color "[36m" args))
(defn white         [& args] (apply-color "[37m" args))

(defn on-grey       [& args] (apply-color "[40m" args))
(defn on-red        [& args] (apply-color "[41m" args))
(defn on-green      [& args] (apply-color "[42m" args))
(defn on-yellow     [& args] (apply-color "[43m" args))
(defn on-blue       [& args] (apply-color "[44m" args))
(defn on-magenta    [& args] (apply-color "[45m" args))
(defn on-cyan       [& args] (apply-color "[46m" args))
(defn on-white      [& args] (apply-color "[47m" args))
