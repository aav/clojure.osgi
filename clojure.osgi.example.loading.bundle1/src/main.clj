(ns main
	(:use clojure.osgi.core)
	(:use clojure.osgi.example.loading.bundle2.other) 
)

(println "main.clj loaded. Bundle symbolic name is: " (.. *bundle* getSymbolicName))




(defn- bundle-start [context]
	(println "Bundle 2 activator is called")

)