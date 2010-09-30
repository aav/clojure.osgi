(ns clojure.osgi.example.loading.aot.CljClass
  (:gen-class))

(defn -toString [] (str "CljClass.class(bundleAOT): Hello !"))