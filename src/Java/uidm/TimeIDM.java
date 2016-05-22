package uidm;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TimeIDM extends UniqueIDM {

	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	private Runtime runtime;
	private String[] ARG;
	
	public TimeIDM() throws IOException {
		super();
		if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ){
			ARG= new String[]{ "/bin/sh", "-c", "date +%s%N" };
		}
		else if(OS.indexOf("win") >= 0){
			
		}else if(OS.indexOf("mac") >= 0){
			
		}else{
			throw new IOException("OS system incompatible");
		}
		this.runtime = Runtime.getRuntime();

	}	
	@Override
	protected long LocalGetIDM() throws IOException {
		final Process process = runtime.exec(this.ARG);
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		return Long.parseUnsignedLong(br.readLine());
	}
	
}