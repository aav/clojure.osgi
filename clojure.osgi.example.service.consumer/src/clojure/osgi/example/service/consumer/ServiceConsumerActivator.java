package clojure.osgi.example.service.consumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;

public class ServiceConsumerActivator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		ClojureOSGi.requireAndStart(context, "clojure.osgi.example.service.consumer.service-consumer");
	}

	public void stop(BundleContext context) throws Exception {
	}
}
