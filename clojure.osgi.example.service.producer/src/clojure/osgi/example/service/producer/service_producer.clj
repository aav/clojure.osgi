(ns clojure.osgi.example.service.producer.service-producer
  (:use (clojure.osgi core services))
)

(defprotocol MyService
  (say-hello [this])
)

(defn- bundle-start [context]
  (println "registering service")
	(register-service MyService {"myProperty" "myValue"} 
	 (say-hello [_] (println "Hello from service!"))
	)
)








