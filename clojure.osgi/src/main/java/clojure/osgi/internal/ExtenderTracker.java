package clojure.osgi.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

import clojure.lang.RT;
import clojure.lang.Var;

public class ExtenderTracker extends BundleTracker {
	private Set<Bundle> requireProcessed = Collections.synchronizedSet(new HashSet<Bundle>());
	private Set<Bundle> active = Collections.synchronizedSet(new HashSet<Bundle>());

	public ExtenderTracker(BundleContext context) {
		super(context, Bundle.RESOLVED | Bundle.ACTIVE | Bundle.STARTING | Bundle.STOPPING, null);
	}

	public Object addingBundle(Bundle bundle, BundleEvent event) {
		if (!requireProcessed.contains(bundle)) {
			processRequire(bundle);
			requireProcessed.add(bundle);
		}

		if ((bundle.getState() == Bundle.STARTING || bundle.getState() == Bundle.ACTIVE) && !active.contains(bundle)) {
			invokeActivatorCallback("bundle-start", bundle);
			active.add(bundle);
		}else
		if((bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STOPPING) && active.contains(bundle)) {
			invokeActivatorCallback("bundle-stop", bundle);
			active.remove(bundle);
		}
		
		return null;
	}

	private void processRequire(Bundle bundle) {
		String header = (String) bundle.getHeaders().get("Clojure-RequireNamespace");

		if (header != null) {
			StringTokenizer st = new StringTokenizer(header, ",");
			while (st.hasMoreTokens()) {
				String ns = st.nextToken().trim();

				ClojureOSGi.require(bundle, ns);
			}
		}
	}

	private void invokeActivatorCallback(String aFunction, final Bundle bundle) {
		String ns = (String) bundle.getHeaders().get("Clojure-ActivatorNamespace");
		if (ns != null) {
			final Var var = RT.var(ns, aFunction);
			if (var.isBound()) {
				try {
					ClojureOSGi.withBundle(bundle, new RunnableWithException() {
						public void run() throws Exception {
							var.invoke(bundle.getBundleContext());
						}
					});
					
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else
				throw new RuntimeException(String.format("'%s' is not bound in '%s'", aFunction, ns));
		}
	}

	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {

	}

	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {

	}
}
