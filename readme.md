# bask

> (bb ask!)

Get user input on the cli.

[![asciicast](https://asciinema.org/a/H9ku50G8la8CJL6lCxnqs1EwC.png)](https://asciinema.org/a/H9ku50G8la8CJL6lCxnqs1EwC)

## Prompt Types


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
       :choices ["red" "green" "blue" "orange"]
       :initial "orange"})
;;=uses fzf to select a color=>
;;=> "orange"
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

## Multiple questions at once

Pass a collection of questions to `ask!`, and it will ask them in sequence, returning a map keyed by `:id`s.

If one of the ids are missing, we use the index of the question as a key, so:

``` clojure
(ask! ["one" {:type :text :msg "two"} "three"])
;= asks 3 questions =>
{0 "apple", 1 "orange", 2 "banana"}
```

# Examples

## Interactive script builder

`bb examples/builder.clj`

## cli asker

`bb examples/ask.clj '["one" "two" "three"]'`
