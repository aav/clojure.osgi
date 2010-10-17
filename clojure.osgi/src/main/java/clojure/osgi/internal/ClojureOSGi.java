package clojure.osgi.internal;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureOSGi {
	static final private Var REQUIRE = RT.var("clojure.core", "require");
	static final private Var OSGI_REQUIRE = RT.var("clojure.osgi.core", "osgi-require");
	static final private Var BUNDLE = RT.var("clojure.osgi.core", "*bundle*");
	private static BundleContext s_Context;
	private static boolean s_Initialized;

	public static void initialize(BundleContext aContext) throws Exception {
		s_Context = aContext;

		if (!s_Initialized) {
			withBundle(s_Context.getBundle(), new RunnableWithException() {
				public void run() throws Exception {
					REQUIRE.invoke(Symbol.intern("clojure.main"));
					REQUIRE.invoke(Symbol.intern("clojure.osgi.core"));
				}
			});

			s_Initialized = true;
		}
	}

	public static void require(Bundle aBundle, final String aName) {
		try {
			withBundle(aBundle, new RunnableWithException() {
				public void run() throws Exception {
					OSGI_REQUIRE.invoke(Symbol.intern(aName));
				}
			});
		} catch (Exception aEx) {
			throw new RuntimeException(aEx);
		}
	}

	public static void loadAOTClass(final BundleContext aContext, final String fullyQualifiedAOTClassName) throws Exception {

		withBundle(aContext.getBundle(), new RunnableWithException() {
			public void run() throws Exception {
				Class.forName(fullyQualifiedAOTClassName, true, new BundleClassLoader(aContext.getBundle()));
			}
		});
	}
	
	public static void load(final String aName, Bundle aBundle) {

		try {
			URL url = aBundle.getResource(aName + ".clj");
			if (url != null) {
				BundleIdExtractor extractor = new EquinoxBundleIdExtractor();

				Bundle bundle = s_Context.getBundle(extractor.extractBundleId(url));

				withBundle(bundle, new RunnableWithException() {
					public void run() throws Exception {
						RT.load(aName);
					}
				});
			} else
				RT.load(aName);
		} catch (Exception aEx) {
			throw new RuntimeException(aEx);
		}
	}

	public static void withBundle(Bundle aBundle, RunnableWithException aCode) throws Exception {
		ClassLoader loader = new BundleClassLoader(aBundle);
		IPersistentMap bindings = RT.map(BUNDLE, aBundle, Compiler.LOADER, loader);

		boolean pushed = true;

		ClassLoader saved = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(loader);

			try {
				Var.pushThreadBindings(bindings);
			} catch (Exception aEx) {
				pushed = false;
				throw aEx;
			}

			aCode.run();
		} finally {
			if (pushed)
				Var.popThreadBindings();

			Thread.currentThread().setContextClassLoader(saved);
		}
	}
}
