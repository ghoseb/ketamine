(ns ^{:doc "Ketamine - Consistent hashing for Clojure."
      :author "Baishampayan Ghose <b.ghose@helpshift.com>"}
  ketamine.core
  (:require [ketamine.util :refer [hash-code gen-keyval-map]]))

(defprotocol ^:private IConsistentHashRing
  (-add-node [hash-ring node] "Add a new node to the hash ring.")
  (-remove-node [hash-ring node] "Remove a node from the hash ring.")
  (-get-node [hash-ring key] "Given a string key get the first node and its position in the ring.")
  (-node-seq [hash-ring key] "Given a string key return a ordered sequence of all nodes that could contain the key."))


(defrecord ^:private HashRing
  [ring sorted-ks replicas]

  IConsistentHashRing
  (-add-node [_ node]
    (let [kvs (gen-keyval-map node replicas)
          ks (keys kvs)
          ring* (merge ring kvs)
          sorted-ks* (apply conj sorted-ks ks)]
      (->HashRing ring* sorted-ks* replicas)))

  (-remove-node [_ node]
    (let [ks (keys (gen-keyval-map node replicas))
          ring* (reduce dissoc ring ks)
          sorted-ks* (apply disj sorted-ks ks)]
      (->HashRing ring* sorted-ks* replicas)))

  (-get-node [_ key]
    (let [k (hash-code key)
          candidate-nodes (for [[idx node-key] (map vector (range) sorted-ks) :when (<= k node-key)]
                            [(ring node-key) idx])]
      (if (seq candidate-nodes)
        (first candidate-nodes)
        [(ring (first sorted-ks)) 0])))

  (-node-seq [hash-ring key]
    (let [[node idx] (-get-node hash-ring key)]
      (map (partial get ring)
           (concat (drop idx sorted-ks)
                   (cycle sorted-ks))))))

(declare add-node)

(defn make-ring
  "Make a new HashRing."
  ([]
     (make-ring []))
  ([nodes]
     (make-ring nodes 20))
  ([nodes replicas]
     (reduce add-node (->HashRing {} (sorted-set) replicas) nodes)))


(defn add-node
  "Add a node to a consistent hash ring."
  [chash node]
  (-add-node chash node))


(defn remove-node
  "Remove a node from a consistent hash ring."
  [chash node]
  (-remove-node chash node))


(defn get-node
  "Get a node for a given key from a consistent hash ring."
  [chash key]
  (first (-get-node chash key)))


(defn node-seq
  "Get a lazy sequence of candidate nodes for a key from a consistent hash ring."
  [chash key]
  (-node-seq chash key))


(comment
  (def chash (make-ring ["foo" "bar" "baz" "quux"]))
  (add-node chash "frob")
  (remove-node chash "bar")
  (get-node chash "123456")
  (node-seq chash "789012")
)
