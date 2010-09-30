package clojure.osgi.example.loading.bundle2.internal;

public class InternalClass {
	static {
		System.out.println("\nInternalClass.class(bundle2): Hello from static init");
	}
	public static void hello() {
		System.out.println("InternalClass.class(bundle2): Hello from the internal class");
	}
}
