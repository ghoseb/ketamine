(ns ^{:doc "Ketamine utility functions."
      :author "Baishampayan Ghose <b.ghose@helpshift.com>"}
  ketamine.util
  (:require [clojure.string :as cs])
  (:import java.security.MessageDigest))


(def ^:private hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])

(defn ^:private hexify-byte
  [b]
  (let [v (bit-and b 0xFF)]
    [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))

(defn hexify
  "Convert byte-array to hex string"
  [ba]
  (cs/join (mapcat hexify-byte ba)))


(defn sha1sum
  [^String s]
  {:pre [(string? s)]}
  (let  [digest ^MessageDigest (MessageDigest/getInstance "SHA-1")]
    (.reset digest)
    (hexify (.digest digest
                     (.getBytes ^String s "UTF-8")))))


(defn hash-code
  "Hash a string and convert the hash code into a number in 64 bit address space."
  [^String x]
  (-> x
      sha1sum
      (subs 0 8)
      (Long/parseLong 16)))


(defn gen-keyval-map
  "Given a node and the number of replicas generate a map of hash code and node for the ring."
  [node replicas]
  (into {}
        (for [i (range replicas) :let [k (hash-code (str node ":" i))]]
          [k node])))


(defn indexed
  "Given a coll return a indexed version thereof."
  [coll]
  (map vector (range) coll))
