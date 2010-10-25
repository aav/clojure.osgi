(defproject clojure.osgi.example.loading.aot "1.0.0.qualifier"
  :description "test bundle for clojure.osgi and aot"
  :url "http://github.com/aav/clojure.osgi"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :aot [clojure.osgi.example.loading.aot.CljClass])
