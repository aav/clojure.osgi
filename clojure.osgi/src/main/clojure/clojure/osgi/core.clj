(ns clojure.osgi.core
)

(def *bundle* nil)

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

(defn- osgi-load [bundle]
  (fn [path]
	  (let [^String path (if (.startsWith path "/")
	    path
	    (str (root-directory (ns-name *ns*)) \/ path))]
	
		  (if-not (*pending-paths* path)
		    (do
				  (binding [*pending-paths* (conj *pending-paths* path)]
				    (clojure.osgi.internal.ClojureOSGi/load  (.substring path 1) bundle)
		 	    )
		    )
		  )
	  )
  )
)

(defn bundle-name []
  (.getSymbolicName *bundle*)
) 

(defn bundle-class-loader [bundle]
  (clojure.osgi.internal.BundleClassLoader. bundle)
)

(defn with-bundle* [bundle function]
  (binding [*bundle* bundle load (osgi-load bundle)]
     (clojure.osgi.internal.ClojureOSGi/withLoader 
       (bundle-class-loader bundle) function)
  )   
)

(defmacro with-bundle [bundle & body]
  `(with-bundle* ~bundle
      (fn [] ~@body)
   )
)