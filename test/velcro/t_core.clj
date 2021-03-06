(ns velcro.t-core
  (:require
   [velcro.core :refer :all]

   [midje.sweet :refer :all]))

(facts "about 1 node replacement"
  (fact "replaces current node by the given one"
    (replace-in '(1 + 2)
                [current-node]
                (by (fn [_] '-))
                (where #(= (current-node %) '+))) => '(1 - 2))

  (fact "replaces left node by the given one"
    (replace-in '(1 + 2)
                [left-node]
                (by inc)
                (where #(= (current-node %) '+))) => '(2 + 2))

  (fact "replaces right node by the given one"
    (replace-in '(1 + 2)
                [right-node]
                (by dec)
                (where #(= (current-node %) '+))) => '(1 + 1))

  (fact "prevents infinite recursion"
    (replace-in '(1 + 2)
                [current-node]
                (by (fn [_] (list 3 4 '+)))
                (where #(= (current-node %) '+)))))

(facts "about 2 nodes replacement"
  (fact "replaces left node and current node by the given one"
    (replace-in '(1 2 3)
                [left-node current-node]
                (by (fn [e _] e))
                (where #(= (current-node %) 2))) => '(1 3))

  (fact "replaces right node and current node by the given one"
    (replace-in '(1 2 3)
                [right-node current-node]
                (by (fn [e _] e))
                (where #(= (current-node %) 2))) => '(1 3))
  
  (fact "replaces current node and left node by the given one"
    (replace-in '(1 2 3)
                [current-node left-node]
                (by (fn [_ e] e))
                (where #(= (current-node %) 2))) => '(1 3))

  (fact "replaces current node and right node by the given one"
    (replace-in '(1 2 3)
                [current-node right-node]
                (by (fn [_ e] e))
                (where #(= (current-node %) 2))) => '(1 3)))

(facts "about 3 nodes replacement"
  (fact "replaces left, right and current nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [left-node right-node current-node]
                (by (fn [actual expected _] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2)))

  (fact "replaces left, current and right nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [left-node current-node right-node]
                (by (fn [actual _ expected] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2)))

  (fact "replaces right, left and current nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [right-node left-node current-node]
                (by (fn [expected actual _] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2)))

  (fact "replaces right, current and left nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [right-node current-node left-node]
                (by (fn [expected _ actual] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2)))

  (fact "replaces current, right and left nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [current-node right-node left-node]
                (by (fn [_ expected actual] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2)))

  (fact "replaces current, left and right nodes by the given one"
    (replace-in '(test "testing" 1 -> 2)
                [current-node left-node right-node]
                (by (fn [_ actual expected] (list '= actual expected)))
                (where #(= (current-node %) '->))) => '(test "testing" (= 1 2))))

(facts "about up node"
  (fact "replaces up node by the given one"
    (replace-in '((println "begin") (my-let [a 1 b 2] (+ a b)) (println "done"))
                [up-node]
                (by (fn [form] `(let ~(second form) ~(first (drop 2 form)) (println "wrapped"))))
                (where #(= (current-node %) 'my-let))) => '((println "begin") (clojure.core/let [a 1 b 2] (+ a b) (clojure.core/println "wrapped")) (println "done"))))
