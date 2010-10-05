package clojure.osgi.example.loading.bundle1;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;

public class Bundle1Activator implements BundleActivator {
	public void start(BundleContext bundleContext) throws Exception {
		ClojureOSGi.requireAndStart(bundleContext, "main");
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
}
