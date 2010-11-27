(ns clojure.osgi.example.loading.aot.main
	(:require [clojure.osgi.core :as osgi])
)

;(println "main.clj(bundleAOT): loaded. Bundle symbolic name is:" (.getSymbolicName osgi/*bundle*))

(defn- bundle-start [context]
	(println "main.clj(bundleAOT): bundle-start is called")
  (try
    (osgi/load-aot-class context "clojure.osgi.example.loading.aot.CljClass")
    (println "\n" (clojure.osgi.example.loading.aot.CljClass.))
    (println "BundleAOTActivator.class: instanciation of class CljClass worked as expected")
    (catch Exception e
      (println "BundleAOTActivator.class: unexpected fail of instanciation for class CljClass"))))

(defn- bundle-stop [context]
	(println "main.clj(bundleAOT): bundle-stop is called")
)

(defn hello []  "hello from bundleAOT") ;; TODO remove ? 
