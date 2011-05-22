package clojure.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public interface IClojureOSGi {
	void require(Bundle aBundle, final String aName);
	void loadAOTClass(final Bundle aContext, final String fullyQualifiedAOTClassName) throws Exception;
}
