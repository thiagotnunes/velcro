(ns velcro.core
  (:require
   [clojure.zip :as zip]))

(defn- remove-left [form]
  (-> form
      zip/left
      zip/remove
      zip/next))

(defn- remove-right [form]
  (-> form
      zip/right
      zip/remove))

(defn- insert-current [form node]
  (-> form
      (zip/insert-left node)
      zip/remove))

(defn- insert-left [form node]
  (-> form
      (zip/insert-left node)))

(defn- insert-right [form node]
  (-> form
      (zip/insert-right node)
      zip/next))

(defn- insert-up [form node]
  (-> form
      zip/up
      (zip/insert-left node)
      zip/remove))

(defn left-node [form]
  (-> form zip/left zip/node))

(defn right-node [form]
  (-> form zip/right zip/node))

(defn current-node [form]
  (zip/node form))

(defn up-node [form]
  (-> form zip/up zip/node))

(def node-fn-mapping {left-node    {:order 0
                                    :remove-fn remove-left
                                    :insert-fns {:head insert-left
                                                 :tail insert-left}}
                      right-node   {:order 1
                                    :remove-fn remove-right
                                    :insert-fns {:head insert-right
                                                 :tail insert-right}}
                      current-node {:order 2
                                    :remove-fn identity
                                    :insert-fns {:head insert-current
                                                 :tail insert-right}}
                      up-node      {:order 3
                                    :remove-fn identity
                                    :insert-fns {:head insert-up
                                                 :tail insert-right}}})

(defn by [func]
  (fn [nodes]
    (fn [form {head-fn :head}]
      (let [replacement (apply func nodes)]
        (head-fn form replacement)))))

(defn append-to [form nodes insert-fn]
  (if (seq nodes)
    (recur (insert-fn form (first nodes)) (rest nodes) insert-fn)
    form))

(defn by-spliced [func]
  (fn [nodes]
    (fn [form {head-fn :head tail-fn :tail}]
      (let [replacement (apply func nodes)]
        (append-to (head-fn form (first replacement))
                   (rest replacement)
                   tail-fn)))))

(defn where [fn]
  fn)

(defn- remove-nodes [form nodes-fn]
  (let [remove-fn (->> nodes-fn
                       (map #(node-fn-mapping %))
                       (sort-by :order)
                       (map :remove-fn)
                       (apply comp))]
    (remove-fn form)))

(defn- insert-node [form replacement-fn nodes-fn]
  (let [insert-fn (->> nodes-fn
                       (map #(node-fn-mapping %))
                       (sort-by :order)
                       (map :insert-fns)
                       last)]
    (replacement-fn form insert-fn)))

(defn replace-in [form nodes-fn by-fn where-fn]
  (letfn [(replace-in-form [form]
            (if (zip/end? form)
              form
              (if (where-fn form)
                (let [nodes ((apply juxt nodes-fn) form)
                      replacement-fn (by-fn nodes)]
                  (recur (-> form
                             (remove-nodes nodes-fn)
                             (insert-node replacement-fn nodes-fn)
                             zip/next)))
                (recur (zip/next form)))))]
    (-> form
        zip/seq-zip
        replace-in-form
                zip/root)))
