package clojure.osgi.internal;

import java.net.URL;

public interface BundleIdExtractor {
	long extractBundleId(URL aURL);
}
