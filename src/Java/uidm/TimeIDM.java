package uidm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TimeIDM extends UniqueIDM {

	private Runtime runtime;
	private final String[] UNIX_ARG = { "/bin/sh", "-c", "date +%s%N" };

	public TimeIDM() {
		super();
		this.runtime = Runtime.getRuntime();

	}

	protected long LocalGetIDM() throws IOException {

		final Process process = runtime.exec(this.UNIX_ARG);
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		return Long.parseUnsignedLong(br.readLine());

	}

}
