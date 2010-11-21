package clojure.osgi.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import clojure.lang.Compiler;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class ClojureOSGi {
	static final private Var REQUIRE = RT.var("clojure.core", "require");
	static final private Var WITH_BUNDLE = RT.var("clojure.osgi.core", "with-bundle*");
	static final private Var BUNDLE = RT.var("clojure.osgi.core", "*bundle*");
	
	private static boolean s_Initialized;

	public static void initialize(final BundleContext aContext) throws Exception {
		if (!s_Initialized) {
			withLoader(ClojureOSGi.class.getClassLoader(), new RunnableWithException() {
				public void run() {
					boolean pushed = false;
					
					try {
						REQUIRE.invoke(Symbol.intern("clojure.main"));
						
						Var.pushThreadBindings(RT.map(BUNDLE, aContext.getBundle()));
						pushed = true;
						
						REQUIRE.invoke(Symbol.intern("clojure.osgi.core"));
					} catch (Exception e) {
						throw new RuntimeException("cannot initialize clojure.osgi", e);
					}finally{
						if(pushed)
							Var.popThreadBindings();
					}
				}
			});

			s_Initialized = true;
		}
	}

	public static void require(Bundle aBundle, final String aName) {
		try {
			withBundle(aBundle, new RunnableWithException() {
				public void run() throws Exception {
					REQUIRE.invoke(Symbol.intern(aName));
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
	
	private static void withLoader(ClassLoader aLoader, RunnableWithException aRunnable) throws Exception {
		try {
			Var.pushThreadBindings(RT.map(Compiler.LOADER, aLoader));
			aRunnable.run();
		}
		finally {
			Var.popThreadBindings();
		}
	}
	
	public static void withLoader(ClassLoader aLoader, final Runnable aRunnable) throws Exception {
		withLoader(aLoader, new RunnableWithException() {
			public void run() throws Exception {
				aRunnable.run();
			}
		});
	}
	
	public static void withBundle(Bundle aBundle, final RunnableWithException aCode) throws Exception {
		WITH_BUNDLE.invoke(aBundle, new Runnable() {
			public void run() {
				try {
					aCode.run();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
}
