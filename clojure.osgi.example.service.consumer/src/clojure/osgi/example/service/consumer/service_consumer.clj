(ns clojure.osgi.example.service.consumer.service-consumer
  (:use
    (clojure.osgi core services filters)
    (clojure.osgi.example.service.producer service-producer)
  )
  (:import 
		(org.osgi.util.tracker ServiceTrackerCustomizer)
	)
)


(defn- bundle-start [context]
  (println "tracking service")

  (track-service MyService
    (reify ServiceTrackerCustomizer
      (addingService [_ reference]
        (let [service (.getService context reference)]
          (say-hello service)
          service            
        )
      )

      (removedService [_ reference service]
        (println "Service removed")
        (.ungetService context reference)
      )

      (modifiedService [_ reference service]
      )
    )
  )
)
