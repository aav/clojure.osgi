(ns main
	(:use clojure.osgi.core)
	(:use clojure.osgi.example.loading.bundle2.other) 
)

(println "main.clj(bundle1): loaded. Bundle symbolic name is:" (.. *bundle* getSymbolicName))

(defn- bundle-start [context]
	(println "main.clj(bundle1): bundle-start is called")
)

(defn- bundle-stop [context]
	(println "main.clj(bundle1): bundle-stop is called")
)


(try
  (use 'clojure.osgi.example.loading.bundle2.internal.private)
  (throw (RuntimeException."main.clj(bundle1): trying to use a namespace from internal package of bundle2 should have failed"))  
  (catch Exception e
    (println "main.clj(bundle1): trying to use a namespace from internal package of bundle2 failed as expected")))