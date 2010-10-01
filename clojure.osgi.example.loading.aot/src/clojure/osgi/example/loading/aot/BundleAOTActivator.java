package clojure.osgi.example.loading.aot;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import clojure.osgi.ClojureOSGi;
import clojure.osgi.ClojureOSGi.RunnableWithException;

public class BundleAOTActivator implements BundleActivator {
	public void start(final BundleContext bundleContext) throws Exception {
		try {
			ClojureOSGi.withBundle(bundleContext.getBundle(), new RunnableWithException() {
				public void run() throws Exception {
					bundleContext.getBundle().loadClass("clojure.osgi.example.loading.aot.CljClass");
				}
			});
			new CljClass();
			System.out.println("BundleAOTActivator.class: expected the load of CljClass to fail");
		} catch (Exception e) {
			System.out.println("BundleAOTActivator.class: instanciation failed as expected");
			throw e;
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
	
	public static String hello() { return "hello from bundleATO"; }
}
