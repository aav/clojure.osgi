(ns clojure.osgi.filters
	(:use (clojure.contrib macro-utils))  
)

(defn- osgi-filter* [filter]
  (str "(" filter ")"))

(defn osgi-filter-and [& filter-list]
  (osgi-filter* (str "&" (apply str (interpose " " filter-list)))))

(defn osgi-filter-or [& filter-list]
  (osgi-filter* (str "|" (apply str (interpose " " filter-list)))))

(defn osgi-filter-not [filter]
  (str "!" filter))

(defn osgi-filter-equal [k v]
  (osgi-filter* (str  k "=" v)))

(defmacro osgi-filter [& body]
  	`(macrolet 
  		[
   			(~'=  [k# v#]  `(osgi-filter-equal ~k# ~v#))
        (~'not [f#]    `(osgi-filter-not ~f#))
        (~'or  [& fs#] `(osgi-filter-or ~@fs#)) 
        (~'and [& fs#] `(osgi-filter-and ~@fs#)) 
   		] 
    
     	~@body
   )
)


