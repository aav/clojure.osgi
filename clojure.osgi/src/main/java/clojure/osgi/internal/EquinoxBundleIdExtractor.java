package clojure.osgi.internal;

import java.net.URL;

public class EquinoxBundleIdExtractor implements BundleIdExtractor {

	public long extractBundleId(URL aURL) {
		String host = aURL.getHost();
		
		int dotIndex = host.indexOf('.');
		
		return (dotIndex >= 0 && dotIndex < host.length() - 1) ? Long.parseLong(host.substring(0, dotIndex)) : Long.parseLong(host);
	}
}
