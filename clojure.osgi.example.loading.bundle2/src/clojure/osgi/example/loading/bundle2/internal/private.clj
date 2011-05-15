(ns clojure.osgi.example.loading.bundle2.internal.private
	(:use clojure.osgi.core)
)

(println "private.clj(bundle2): loaded. Bundle symbolic name is:" (.. *bundle* getSymbolicName))