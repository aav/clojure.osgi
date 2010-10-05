package clojure.osgi.example.service.producer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;

public class ServiceProducerActivator implements BundleActivator {

	public void start(BundleContext bundleContext) throws Exception {
		ClojureOSGi.requireAndStart(bundleContext,
				"clojure.osgi.example.service.producer.service-producer");
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
}
