(ns clojure.osgi.example.loading.aot.CljClass
  (:gen-class)
  (:use clojure.osgi.core))

(defn -toString [this] (str "CljClass.class(bundleAOT): dynamic eval:" 
                         (eval (read-string "(do (println \"bundle:\" clojure.osgi.core/*bundle*)(clojure.osgi.example.loading.aot.TestClass/hello))"))))

(println "clojure.osgi.example.loading.aot.CljClass namespace: loaded")