(ns clojure.osgi.example.loading.aot.CljClass
  (:gen-class))

(defn -toString [this] (str "CljClass.class(bundleAOT): Hello !" 
                         (clojure.osgi.example.loading.aot.TestClass/hello)))

(println "clojure.osgi.example.loading.aot.CljClass namespace: loaded")