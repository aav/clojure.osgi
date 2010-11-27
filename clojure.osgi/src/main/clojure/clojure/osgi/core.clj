(ns clojure.osgi.core
(:import [clojure.osgi.internal BundleClassLoader]))

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


(defn bundle-name []
  (.getSymbolicName *bundle*)
) 

(defn bundle-class-loader [bundle]
  (BundleClassLoader. bundle)
)

(declare osgi-load)

(defn with-bundle* [bundle function & params]
  (binding [*bundle* bundle load (osgi-load bundle)]
     (clojure.osgi.internal.ClojureOSGi/withLoader 
       (bundle-class-loader bundle) 
       (if (seq? params)
         (apply partial (cons function params)) function)
     )
  )   
)

(defmacro with-bundle [bundle & body]
  `(with-bundle* ~bundle
      (fn [] ~@body)
   )
)


(defmulti bundle-for-resource (constantly (System/getProperty "org.osgi.framework.vendor")))

(defmethod bundle-for-resource "Eclipse" [bundle resource]
  (letfn 
    [
      (bundle-id [url]
				(let [host (.getHost url) dot (.indexOf host  (int \.))]
				  (Integer/parseInt
				    (if (and (>= dot 0) (< dot (- (count host) 1)))
				      (.substring host 0 dot)
				      host
				    ) 
				  )
				)
      )
    ]

	  (if-let [url (.getResource bundle resource)]
	    (.getBundle (.getBundleContext bundle) (bundle-id url))
	  )
  )
)

(defn load-aot-class [bundle-context class-name]
  (with-bundle (.getBundle bundle-context)
    (Class/forName class-name 
                   true 
                   (BundleClassLoader. 
                     (.getBundle bundle-context))))
) 

(defn- osgi-load [bundle]
  (fn [path]
	  (let [^String path (if (.startsWith path "/")
	    path
	    (str (root-directory (ns-name *ns*)) \/ path))]
	
		  (if-not (*pending-paths* path)
			  (binding [*pending-paths* (conj *pending-paths* path)]
          (let [load (fn [] (clojure.lang.RT/load (.substring path 1)))]
					  (if-let [bundle (bundle-for-resource bundle (str path ".clj"))]
              (with-bundle* bundle load)					    
					    (load))
          )
	 	    )
		  )
	  )
  )
)




