(ns clojure.osgi.core
  (:use    clojure.osgi.services)
  (:import [clojure.osgi.internal BundleClassLoader])
  (:import [clojure.osgi IClojureOSGi])
)


(def ^{:private true} osgi-debug false)

(def *bundle*)

; copy from clojure.core BEGIN
(defn- libspec?
  "Returns true if x is a libspec"
  [x]
  (or (symbol? x)
      (and (vector? x)
           (or
            (nil? (second x))
            (keyword? (second x))))))

(defn- prependss
  "Prepends a symbol or a seq to coll"
  [x coll]
  (if (symbol? x)
    (cons x coll)
    (concat x coll)))

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


(defn- full-path [path]
	(if (.startsWith path "/")
	  path
	  (str (root-directory (ns-name *ns*)) \/ path))
)


(defonce 
  ^{:private true}
  *currently-loading* nil)


(defn bundle-class-loader [bundle]
  (BundleClassLoader. bundle)
)


(let [bundle *bundle*]
	(defn bundle-name []
	  (.getSymbolicName bundle)
	)

	(defn get-bundle [bid]
    (.. bundle (getBundleContext) (getBundle bid)) 
	)
)

(defn- libspecs [args]
  (flatten 
    (map 
	    (fn [arg]
	      (cond
	        (keyword arg) 
	          arg
	
	        (and (vector? arg) (or (nil? (second arg)) (keyword? (second arg)))) 
	          (first arg)
	
	        :default
	          (let [[prefix & args] arg]
	             (map #(str (name prefix) "."  (if (coll? %) (name (first %)) (name %))) args))
	      )
	    )

      (filter (complement keyword?) args)
    )
  )
)

(defn- available [lib]
  (or
     (let [cname (str lib "__init")]
       (try
	       (.loadClass *bundle* cname)
	       (catch ClassNotFoundException _
           (when osgi-debug
             (println "class not found: " cname))
         )
       )
     )
     
     (let [rname (str (root-resource lib) ".clj")] 
	     (or 
         (.getResource *bundle* rname)
         (when osgi-debug
           (println "resource not found: " rname))
       )
     )
  )
)

(defn check-libs [libs]
  (doseq [lib libs]
    (when-not (available lib)
      (when osgi-debug
        (println (str lib " is not available from bundle " (.getSymbolicName *bundle*))))

      (throw (Exception. (str "cannot load " lib " from bundle " (.getSymbolicName *bundle*))))
    )
  )
)

; override clojure.core/use
(let [original use]
	(defn- osgi-use [& args]
	  (when osgi-debug
	 	  (println (str "use " args " from " (.getSymbolicName *bundle*) *currently-loading*)))
	
	  (check-libs (libspecs args))
	
	  (apply original args)
	)
)

(let [original require]
	(defn- osgi-require [& args]
    (when osgi-debug
      (println (str "require " args " from " (.getSymbolicName *bundle*) *currently-loading*)))

	  (check-libs (libspecs args))

	  (apply original args)
	)
)

(declare with-bundle*)
(defmulti bundle-for-resource (constantly (System/getProperty "org.osgi.framework.vendor")))

; extracts bundle id from eclipse/equinox resource url 
(defn- eclipse-bundle-id [url]
	(let [host (.getHost url) dot (.indexOf host  (int \.))]
	  (Integer/parseInt
	    (if (and (>= dot 0) (< dot (- (count host) 1)))
	      (.substring host 0 dot) host))
	)
)

; computes bundle that is able to provide resource, specified by it's name
(defmethod bundle-for-resource "Eclipse" [bundle resource]
  (if-let [url (.getResource bundle resource)]
    (.getBundle (.getBundleContext *clojure-osgi-bundle*) (eclipse-bundle-id url))
  )
)



; OSGi version of clojure's load 
(defn- osgi-load [path]
  (when osgi-debug
    (println (str "load " path " from " (.getSymbolicName *bundle*))))
 
  (let [path (full-path path)]
	  (if-not (*pending-paths* path)
		  (binding [
                *pending-paths* (conj *pending-paths* path)
                *currently-loading* path]
        (let [load (fn [] (clojure.lang.RT/load (.substring path 1)))]
				  (if-let [bundle (bundle-for-resource *bundle* (str path ".clj"))]
            (with-bundle* bundle load)					    
				    (load))
        )
 	    )
	  )
  )
)

; invokes function in the environment set-up for specified bundle:
;   - classloader is is set to appropriate bundle class loader
;   - clojure.core/load is re-bound with osgi-load
;   - clojure.core/use is re-bound with osgi-use
(defn with-bundle* [bundle function & params]
  (binding [*bundle* bundle 
            load osgi-load
            use  osgi-use
            require osgi-require]
     (clojure.osgi.internal.ClojureOSGi/withLoader (BundleClassLoader. bundle) 
       (if (seq? params)
         (apply partial (cons function params)) function)
     )
  )   
)

; convinience macro
(defmacro with-bundle [bundle & body]
  `(with-bundle* ~bundle
      (fn [] ~@body)
   )
)

(register-service IClojureOSGi
   (require [_ bundle name]
     (with-bundle bundle
        (require (symbol name))))

   (loadAOTClass [_ bundle name]
		 (with-bundle bundle
		    (Class/forName name true 
	        (BundleClassLoader. bundle))))
) 

