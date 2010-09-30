package clojure.osgi.example.loading.aot;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BundleAOTActivator implements BundleActivator {
	public void start(BundleContext bundleContext) throws Exception {
		try {
			System.out.println(new CljClass().toString());
			throw new RuntimeException("BundleAOTActivator.class: expected the load of CljClass to fail");
		} catch (Throwable e) {
			System.out.println("BundleAOTActivator.class: load failed as expected");
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
}
