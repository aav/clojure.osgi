package clojure.osgi.internal;

import java.io.PrintStream;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class StreamLog implements LogService {
	private PrintStream stream;

	public StreamLog(PrintStream out) {
		this.stream = out;
	}

	public void log(int severity, String message) {
		stream.println(String.format("%d - %s", severity, message));
	}

	public void log(int severity, String message, Throwable exception) {
		log(severity, message);
		exception.printStackTrace(stream);
	}

	public void log(ServiceReference service, int severity, String message) {
	}

	public void log(ServiceReference service, int severity, String message, Throwable exception) {
	}
}
