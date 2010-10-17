(ns clojure.osgi.core
)

; copy from clojure.core BEGIN
(defn- root-resource
  "Returns the root directory path for a lib"
  {:tag String}
  [lib]
  (str \/
       (.. (name lib)
           (replace \- \_)
           (replace \. \/))))

(defn- root-directory
  "Returns the root resource path for a lib"
  [lib]
  (let [d (root-resource lib)]
    (subs d 0 (.lastIndexOf d "/"))))


(defonce
  ^{:private true
    :doc "the set of paths currently being loaded by this thread"}
  *pending-paths* #{})

; copy from clojure.core - END

(defn osgi-load [path]
  (let [^String path (if (.startsWith path "/")
    path
    (str (root-directory (ns-name *ns*)) \/ path))]

	  (if-not (*pending-paths* path)
	    (do
			  (binding [*pending-paths* (conj *pending-paths* path)]
			    (clojure.osgi.internal.ClojureOSGi/load  (.substring path 1) *bundle*)
	 	    )
	    )
	  )
  )
)

(defn osgi-require [name]
 (binding [load osgi-load]
   (require name)
 )
)

(defn bundle-name []
  (.getSymbolicName *bundle*)
) 




