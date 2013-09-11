# velcro

Making working with zippers a little bit less painful.

## Usage

Include this in your project.clj:

```clojure
[velcro "0.0.1"]
```

Velcro should be used for replacing elements within a form:

```clojure
(ns my-ns
  (:require
    [velcro.core :refer :all]))

(replace-in '(1 + 2)
            [current-node]
            (by (fn [_] '-))
            (where #(= (current-node %) '+)))
; => '(1 - 2)

(replace-in '(1 + 2)
            [left-node]
            (by inc)
            (where #(= (current-node %) '+)))
; => '(2 + 2)

(replace-in '(1 + 2)
            [right-node]
            (by dec)
            (where #(= (current-node %) '+)))
; => '(1 + 1)

(replace-in '(test "testing the function" 1 -> 2)
            [left-node current-node right-node]
            (by (fn [actual _ expected] (list '= actual expected)))
            (where #(= (current-node %) '->)))
; => '(test "testing the function" (= 1 2))

(replace-in '((println "begin") (my-let [a 1 b 2] (+ a b)) (println "done"))
            [up-node]
            (by (ƒ [form] `(let ~(second form) ~(first (drop 2 form)) (println "wrapped"))))
            (where #(= (current-node %) 'my-let)))
; => '((println "begin") (clojure.core/let [a 1 b 2] (+ a b) (clojure.core/println "wrapped")) (println "done"))
```

You can also splice the result of your function:

```clojure
(replace-in '(my-funky-let [a 1 b 2] (+ a b))
            [current-node right-node]
            (by-spliced (fn [_ let-body] (list 'let let-body)))
            (where #(= (current-node %) 'my-funky-let)))
; => '(let [a 1 b 2] (+ a b))
```

## License

(The MIT License)

Copyright (c) 2013 Thiago Tasca Nunes

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
