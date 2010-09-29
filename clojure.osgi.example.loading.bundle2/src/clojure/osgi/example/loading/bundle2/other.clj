(ns clojure.osgi.example.loading.bundle2.other
	(:use clojure.osgi.core)
)

(println "other.clj loaded. Bundle symbolic name is: " (.. *bundle* getSymbolicName))


; accesing plain java class which is available only to current bundle
(clojure.osgi.example.loading.bundle2.internal.InternalClass/hello)


