(ns ^{:doc "Ketamine utility functions."
      :author "Baishampayan Ghose <b.ghose@helpshift.com>"}
  ketamine.util
  (:import org.apache.commons.codec.digest.DigestUtils))


(defn hash-code
  "Hash a string and convert the hash code into a number in 64 bit address space."
  [^String x]
  (-> x
      DigestUtils/sha1Hex
      (subs 0 8)
      (Long/parseLong 16)))


(defn gen-keyval-map
  "Given a node and the number of replicas generate a map of hash code and node for the ring."
  [node replicas]
  (into {}
        (for [i (range replicas) :let [k (hash-code (str node ":" i))]]
          [k node])))
