# bask
> (bb ask!)

The quickest and easiset way to get some user input.

## prompt types

### `:text` 

```clojure
;; ask with nothing
(ask!)
;;=asks=> ? :_
;;=> "input"

;; ask with nil (same as above)
(ask! nil)
;;=asks=> ? :_
;;=> "input"

;; ask with a prompt:
(ask! "my q")
;;=asks=> ? My Q:_
;;=> "input"

;; ask and return the result as a map:
(ask! {:id :name})
;;=asks=> ? :_
;;=> {:name "input"}

;; same as above 
(ask! {:id :first-name :type :text})
;;=asks=> ? First name:_
;;=> {:first-name "input"}

;; ask multiple questions in 1 go:
(ask! {:id :first-name} {:id :last-name})
;;=asks=> First name:_
;;=asks=> Last name:_
;;=> {:first-name "input1" :last-name "input2"}

```

### `:number`

Like `:text`, but parses the result is a number. Will continue to ask on invalid input. 

``` clojure
;; ask for a number
(ask! {:type :number :msg "Current age"})
;;=asks=> ? Current age:_
;;=> 23

;; ask for a number
(ask! {:id :age :type :number :msg "Current age"})
;;=asks=> ? Current age:_
;;=> {:age 23}
```

;; :text - prompts for a string
;; :number - like text, but must be parseable, or asks again
;; :select - autocomplete / narrowing for selecting 1 item
;; :multi - autocomplete / narrowing for selecting 0, 1, or many items returned as a vector

### `:select`

``` clojure
;; select from choices
(ask! {:type :select
       :choices ["red" "green" "blue" "orange"]})
;;=uses fzf to select a color=>
;;=> "orange"

;; boolean
(= "true"
   (ask! {:type :select
          :choices ["true" "false"]}))
;;=> true


```



;; (println (pr-str (ask!)))
;; (println (pr-str (ask! "just a string")))
;; (println (pr-str (ask! {:id :apple})))

(ask! {:id :name}
      {:id :age :type :number :msg "Current age?"}
          {:id :color
       :type :select
       :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))}
      {:id :multi-color
       :type :multi
       :choices (vec (shuffle (str/split-lines (slurp "/usr/share/dict/web2"))))})

