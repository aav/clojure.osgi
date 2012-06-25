package clojure.osgi.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.BundleTracker;


public class ClojureOSGiActivator implements BundleActivator {
	private BundleTracker tracker;

	public void start(BundleContext context) throws Exception {
		ClassLoader clojureClassLoader = ClojureOSGiActivator.class.getClassLoader();
		ClassLoader priorClassLoader = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(clojureClassLoader);
			ClojureOSGi.initialize(context);
			tracker = new ExtenderTracker(context);
			tracker.open();
		} finally {
			Thread.currentThread().setContextClassLoader(priorClassLoader);
		}
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();
	}
}
