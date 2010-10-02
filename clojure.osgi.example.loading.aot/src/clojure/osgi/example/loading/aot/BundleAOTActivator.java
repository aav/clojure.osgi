package clojure.osgi.example.loading.aot;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import clojure.osgi.BundleClassLoader;
import clojure.osgi.ClojureOSGi;
import clojure.osgi.RunnableWithException;

public class BundleAOTActivator implements BundleActivator {
	public void start(final BundleContext bundleContext) throws Exception {
		try {
			ClojureOSGi.withBundle(bundleContext.getBundle(), new RunnableWithException() {
				public void run() throws Exception {
					Class.forName(
					        "clojure.osgi.example.loading.aot.CljClass", 
					        true, 
					        new BundleClassLoader(
					                bundleContext.getBundle()));
					System.out.println("\n" + new CljClass().toString());
				}
			});
			System.out.println("BundleAOTActivator.class: instanciation of class CljClass worked as expected");
		} catch (Exception e) {
			System.out.println("BundleAOTActivator.class: unexpected fail of instanciation for class CljClass");
			throw e;
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
	
	public static String hello() { return "hello from bundleAOT"; }
}
