(ns clojure.osgi.example.loading.bundle2.internal.private)

(println "private.clj(bundle2): loaded. Bundle symbolic name is:" (.. *bundle* getSymbolicName))