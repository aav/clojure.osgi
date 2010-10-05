(ns clojure.osgi.example.service.consumer.service-consumer
  (:use
    (clojure.osgi core services filters)
    (clojure.osgi.example.service.producer service-producer)
  )
)




(defn- bundle-start [context]
  (track-service MyService
    (fn [verb reference service]
      (if (= verb :adding)
        (say-hello service)
      )
    )
  )
)
