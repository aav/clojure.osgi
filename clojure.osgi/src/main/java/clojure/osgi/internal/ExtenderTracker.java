package clojure.osgi.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;

import clojure.lang.RT;
import clojure.lang.Var;
import clojure.osgi.RunnableWithException;

public class ExtenderTracker extends BundleTracker {
	private Set<Long> requireProcessed = new HashSet<Long>();
	private Set<Long> active = new HashSet<Long>();
	private ServiceTracker logTracker;
	private LogService log = new StreamLog(System.out);

	private enum CallbackType {
		START, STOP
	}

	public ExtenderTracker(BundleContext context) {
		super(context, Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, null);

		logTracker = new ServiceTracker(context,
				org.osgi.service.log.LogService.class.getName(), null) {
			@Override
			public Object addingService(ServiceReference reference) {
				return log = (LogService) super.addingService(reference);
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				super.removedService(reference, service);

				log = new StreamLog(System.out);
			}
		};

		logTracker.open();
	}

	public Object addingBundle(Bundle bundle, BundleEvent event) {
		if (!requireProcessed.contains(bundle.getBundleId())) {
			processRequire(bundle);
			requireProcessed.add(bundle.getBundleId());
		}

		if ((bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE)
				&& !active.contains(bundle.getBundleId())) {

			try {
				invokeActivatorCallback(CallbackType.START, bundle);
			} finally {
				active.add(bundle.getBundleId());
			}

		} else if (bundle.getState() == Bundle.STOPPING
				&& active.contains(bundle.getBundleId())) {
			try {
				invokeActivatorCallback(CallbackType.STOP, bundle);
			} finally {
				active.remove(bundle.getBundleId());
			}
		}

		return null;
	}

	private void processRequire(Bundle bundle) {
		String header = (String) bundle.getHeaders().get("Clojure-Require");

		if (header != null) {
			StringTokenizer lib = new StringTokenizer(header, ",");
			while (lib.hasMoreTokens()) {
				final String ns = lib.nextToken().trim();
                if (log != null)
					log.log(LogService.LOG_DEBUG, String.format(
							"requiring %s from bundle %s", ns, bundle));
				ClojureOSGi.require(bundle, ns);
			}
		}
	}

    private String callbackFunctionName(CallbackType callback, String header) {
		// TODO support callback function name customization. i.e.:
		// Clojure-ActivatorNamespace:
		// a.b.c.d;start-function="myStart";stop-function="myStop"
		switch (callback) {
		case START:
			return "bundle-start";

		case STOP:
			return "bundle-stop";

		default:
			throw new IllegalStateException();
		}
	}

	private void invokeActivatorCallback(CallbackType callback,
			final Bundle bundle) {
		final String ns = (String) bundle.getHeaders().get(
				"Clojure-ActivatorNamespace");
		if (ns != null) {
			final String callbackFunction = callbackFunctionName(callback, ns);
			final Var var = RT.var(ns, callbackFunction);
            if (var.isBound()) {
				try {
					ClojureOSGi.withBundle(bundle, new RunnableWithException() {
						public Object run() throws Exception {
							if (log != null)
								log.log(LogService.LOG_DEBUG,
										String.format(
												"invoking function %s/%s for bundle: %s",
												ns, callbackFunction, bundle));

							var.invoke(bundle.getBundleContext());
							
							return null;
						}
					});

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException(String.format(
						"'%s' is not bound in '%s'", callbackFunction, ns));

            }
		}
	}

	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {

	}

	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {

	}

	public void close() {
		logTracker.close();
		super.close();
	}

}
