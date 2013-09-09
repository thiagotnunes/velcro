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
  (zip/replace form node))

(defn- insert-left [form node]
  (-> form
      (zip/insert-left node)
      zip/next))

(defn- insert-right [form node]
  (-> form
      (zip/insert-right node)
      zip/next))

(defn left-node [form]
  (-> form zip/left zip/node))

(defn right-node [form]
  (-> form zip/right zip/node))

(defn current-node [form]
  (zip/node form))

(def node-fn-mapping {left-node    {:order 0
                                    :remove-fn remove-left
                                    :insert-fn insert-left}
                      right-node   {:order 1
                                    :remove-fn remove-right
                                    :insert-fn insert-right}
                      current-node {:order 2
                                    :remove-fn identity
                                    :insert-fn insert-current}})

(defn by [func]
  (fn [nodes]
     (apply func nodes)))

(defn where [fn]
  fn)

(defn- remove-nodes [form nodes-fn]
  (let [remove-fn (->> nodes-fn
                       (map #(node-fn-mapping %))
                       (sort-by :order)
                       (map :remove-fn)
                       (apply comp))]
    (remove-fn form)))

(defn- insert-node [form replacement nodes-fn]
  (let [insert-fn (->> nodes-fn
                       (map #(node-fn-mapping %))
                       (sort-by :order)
                       (map :insert-fn)
                       last)]
    (insert-fn form replacement)))

(defn replace-in [form nodes-fn by-fn where-fn]
  (letfn [(replace-in-form [form]
            (if (zip/end? form)
              form
              (if (where-fn form)
                (let [nodes ((apply juxt nodes-fn) form)
                      replacement (by-fn nodes)]
                  (recur (-> form
                             (remove-nodes nodes-fn)
                             (insert-node replacement nodes-fn)
                             zip/next)))
                (recur (zip/next form)))))]
    (-> form
        zip/seq-zip
        replace-in-form
                zip/root)))
