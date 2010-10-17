package clojure.osgi.internal;

import java.util.Collections;
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

public class ExtenderTracker extends BundleTracker {
	private Set<Bundle> requireProcessed = Collections.synchronizedSet(new HashSet<Bundle>());
	private Set<Bundle> active = Collections.synchronizedSet(new HashSet<Bundle>());
	private ServiceTracker logTracker;
	private LogService log;

	private enum CallbackType {
		START, STOP
	};

	public ExtenderTracker(BundleContext context) {
		super(context, Bundle.RESOLVED | Bundle.ACTIVE | Bundle.STARTING | Bundle.STOPPING, null);

		logTracker = new ServiceTracker(context, org.osgi.service.log.LogService.class.getName(), null) {
			@Override
			public Object addingService(ServiceReference reference) {
				return log = (LogService) super.addingService(reference);
			}

			@Override
			public void removedService(ServiceReference reference, Object service) {
				super.removedService(reference, service);

				log = null;
			}
		};

		logTracker.open();
	}

	public Object addingBundle(Bundle bundle, BundleEvent event) {
		if (!requireProcessed.contains(bundle)) {
			processRequire(bundle);
			requireProcessed.add(bundle);
		}

		if ((bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE) && !active.contains(bundle)) {
			invokeActivatorCallback(CallbackType.START, bundle);

			active.add(bundle);
		} else if ((bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STOPPING) && active.contains(bundle)) {
			invokeActivatorCallback(CallbackType.STOP, bundle);
			active.remove(bundle);
		}

		return null;
	}

	private void processRequire(Bundle bundle) {
		String header = (String) bundle.getHeaders().get("Clojure-Require");

		if (header != null) {
			StringTokenizer lib = new StringTokenizer(header, ",");
			while (lib.hasMoreTokens()) {
				String ns = lib.nextToken().trim();

				if (log != null)
					log.log(LogService.LOG_DEBUG, String.format("requiring %s from bundle %s", ns, bundle));

				ClojureOSGi.require(bundle, ns);
			}
		}
	}

	private String callbackFunctionName(CallbackType callback, String header) {
		//TODO support callback function name customization. i.e.: Clojure-ActivatorNamespace: a.b.c.d;start-function="myStart";stop-function="myStop"
		switch (callback) {
		case START:
			return "bundle-start";

		case STOP:
			return "bundle-stop";
			
		default:
			throw new IllegalStateException();
		}
	}
	
	private void invokeActivatorCallback(CallbackType callback, final Bundle bundle) {
		final String ns = (String) bundle.getHeaders().get("Clojure-ActivatorNamespace");
		if (ns != null) {
			final String callbackFunction = callbackFunctionName(callback, ns);
			final Var var = RT.var(ns, callbackFunction);
			if (var.isBound()) {
				try {
					ClojureOSGi.withBundle(bundle, new RunnableWithException() {
						public void run() throws Exception {
							if (log != null)
								log.log(LogService.LOG_DEBUG, String.format("invoking function %s/%s for bundle: %s", ns, callbackFunction, bundle));

							var.invoke(bundle.getBundleContext());
						}
					});

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else
				throw new RuntimeException(String.format("'%s' is not bound in '%s'", callbackFunction, ns));
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
