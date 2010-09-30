(ns clojure.osgi.example.loading.bundle2.other
	(:use clojure.osgi.core)
    (:import [clojure.osgi.example.loading.bundle2.internal InternalClass]) 
)
(println "other.clj(bundle2): loaded. Bundle symbolic name is:" (.. *bundle* getSymbolicName))


; accessing plain java class which is available only to current bundle
(InternalClass/hello)


