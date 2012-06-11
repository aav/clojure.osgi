package clojure.osgi;

import org.osgi.framework.Bundle;

public interface IClojureOSGi {
	void unload(Bundle aBundle);
	void require(Bundle aBundle, final String aName);
	void loadAOTClass(final Bundle aContext, final String fullyQualifiedAOTClassName) throws Exception;
	Object withBundle(Bundle aBundle, final RunnableWithException aCode);
}
