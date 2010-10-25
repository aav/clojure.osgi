(ns clojure.osgi.services
  (:use (clojure.osgi core filters))
  (:import 
		(org.osgi.util.tracker ServiceTracker ServiceTrackerCustomizer)
	)
)

;name of java interface that corresponds to given protocol
(defn protocol-interface-name [protocol]
	{:pre [(map? protocol)]}

  (.getName (:on-interface protocol))
)

(defn- map-to-properties [m]
	{:pre [(map? m)]}

	(let [result (java.util.Properties.)]
		(doseq [[k v] m]
			(.put result k v)
	  )

		result
	)
)

(extend-protocol FilterProtocol
  clojure.lang.PersistentArrayMap ; assuming protocol is represented by a map
  (get-filter [p]
    (get-filter 
      (osgi-filter
        (= "objectClass" (protocol-interface-name p))))
  )

  java.lang.Class
  (get-filter [c]
    (get-filter
      (osgi-filter
        (= "objectClass" (.getName c))))
  )
)

(defprotocol TrackerDestination
	(adding [callback reference service])
	(removed [callback reference service])
	(modified [clallback reference service])
)

(defn track-service [filter customizer]
  (let 
    [context (.getBundleContext *bundle*)
     tracker (ServiceTracker. context (get-filter filter) customizer)]
    
   	 (.open tracker)

   	 tracker
  )
)

(defn register-service* [protocol options service] 
  (let [context (.getBundleContext *bundle*)]
    (.registerService context (protocol-interface-name protocol) service (map-to-properties options))    
  )
)

(defmacro register-service [protocol & methods]
	(let [options (if (map? (first methods)) (first methods) {})
        methods (if (map? (first methods)) (next  methods) methods)]

	   `(register-service* ~protocol ~options
		   (reify ~protocol
	     		~@methods   
		   )
	   )
	 )
)




