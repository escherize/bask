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
(ask! [{:id :first-name} {:id :last-name}])
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

The order of the :choices key will decide what initially has focus.

``` clojure
;; select from choices
(ask! {:type :select
       :choices ["red" "green" "blue" "orange"]})
;;=uses fzf to select a color=>
;;=> "orange"

;; boolean with true highlighted
(= "true" (ask! {:type :select :initial "true" :choices ["true" "false"]}))
;;=hit enter=>
;;=> true

;; boolean with false highlighted
(= "true" (ask! {:type :select :initial "true" :choices ["false" "true"]}))
;;=hit enter=>
;;=> false
```

### `:multi` (select)

Similar to `:select`, but can select multiple choices with `<tab>` or `<shift + space>`.

``` clojure
;; select from choices
(ask! {:id :colors
       :type :select
       :choices ["red" "green" "blue" "orange"]})
;;=uses fzf to select some colors=>
;;=> {:colors ["red" "orange"]}
```


## Roadmap

- interactive demo / intro
- script builder
