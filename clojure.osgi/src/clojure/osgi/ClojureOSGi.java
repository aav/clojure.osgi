package clojure.osgi;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import clojure.lang.Compiler;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import clojure.osgi.internal.BundleIdExtractor;
import clojure.osgi.internal.ClojureOSGiActivator;
import clojure.osgi.internal.EquinoxBundleIdExtractor;

public class ClojureOSGi {
	final static private Var REQUIRE = RT.var("clojure.core", "require");
	final static private Var OSGI_REQUIRE = RT.var("clojure.osgi.core",
			"osgi-require");
	final static private Var BUNDLE = RT.var("clojure.osgi.core", "*bundle*");

	static {
		try {
			REQUIRE.invoke(Symbol.intern("clojure.main"));
			REQUIRE.invoke(Symbol.intern("clojure.osgi.core"));
		} catch (Exception aEx) {
			throw new RuntimeException(aEx);
		}
	}

	public static void require(BundleContext aContext, final String aName)
			throws Exception {

		try {

			withBundle(aContext.getBundle(), new RunnableWithException() {
				public void run() throws Exception {
					OSGI_REQUIRE.invoke(Symbol.intern(aName));
				}
			});
		} catch (Exception aEx) {
			throw aEx;
		}
	}

	public static void requireAndStart(final BundleContext aContext,
			final String aNamespace) throws Exception {

		try {
			withBundle(aContext.getBundle(), new RunnableWithException() {
				public void run() throws Exception {
					OSGI_REQUIRE.invoke(Symbol.intern(aNamespace));

					String name = "bundle-start";
					final Var var = RT.var(aNamespace, name);
					if (var.isBound())
						var.invoke(aContext);
					else
						throw new Exception(String.format(
								"'%s' is not bound in '%s'", name, aNamespace));
				}
			});
		} catch (Exception aEx) {
			throw aEx;
		}
	}

	public static void load(final String aName, Bundle aBundle)
			throws Exception {

		try {

			URL url = aBundle.getResource(aName + ".clj");
			if (url != null) {
				BundleIdExtractor extractor = new EquinoxBundleIdExtractor();

				BundleContext context = ClojureOSGiActivator.getContext();

				assert context != null;

				Bundle bundle = context.getBundle(extractor
						.extractBundleId(url));

				withBundle(bundle, new RunnableWithException() {
					public void run() throws Exception {
						RT.load(aName);
					}
				});
			} else
				RT.load(aName);
		} catch (Exception aEx) {
			throw aEx;
		}
	}

	private static void withBundle(Bundle aBundle, RunnableWithException aCode)
			throws Exception {
		ClassLoader loader = new BundleClassLoader(aBundle);
		IPersistentMap bindings = RT.map(BUNDLE, aBundle, Compiler.LOADER,
				loader);

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

	private interface RunnableWithException {
		void run() throws Exception;
	}

	private static class BundleClassLoader extends ClassLoader {
		private Bundle _bundle;

		public BundleClassLoader(Bundle bundle) {
			_bundle = bundle;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			return _bundle.loadClass(name);
		}

		@Override
		public URL getResource(String name) {
			return _bundle.getResource(name);
		}
	}
}
