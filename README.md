# Ketamine

A consistent hashing library for Clojure that can be used to allocate
resources to different servers. If any server goes down or a new one is
added there is a minimum impact on existing resources as they are mapped
*consistently* to the servers.

Based on [Ketama algorithm](http://dl.acm.org/citation.cfm?id=258660) by
Karger, et al.


## Usage

```Clojure
(require '[ketamine.core :as ketama])

;; let's define a consistent hash ring with some servers
(def chash (atom (ketama/make-ring ["192.168.0.1:1337"
                                    "192.168.0.2:1337"
                                    "192.168.0.3:1337"
                                    "192.168.0.4:1337"])))

;; let's figure out where the resource named "resource-1" should go
(ketama/get-node @chash "resource-1")
;;=> "192.168.0.1:1337"

;; what about "resource-142"?
(ketama/get-node @chash "resource-142")
;;=> "192.168.0.4:1337"

;; and "resource-1kajillion"?
(ketama/get-node @chash "resource-1kajillion")
;;=> "192.168.0.3:1337"

;; now "192.168.0.4:1337" goes down, so let's remove it from the ring
(swap! chash ketama/remove-node "192.168.0.4:1337")

;; let's add a new server to the ring
(swap! chash ketama/add-node "10.10.10.42:1234")

;; so where should "resource-142" go now?
(ketama/get-node @chash "resource-142")
;;=> "10.10.10.42:1234" ; OK

;; and what about the other resources?
(ketama/get-node @chash "resource-1")
;;=> "192.168.0.1:1337"

(ketama/get-node @chash "resource-1kajillion")
;;=> "192.168.0.3:1337"

;; Voila! Consistent hashing ftw.

```

## Contributing

Pull requests are more than welcome! Namasté.

## License

Copyright © 2013 Baishampayan Ghose

Distributed under the Eclipse Public License, the same as Clojure.
