package uidm;
import java.io.IOException;

public abstract class UniqueIDM {
	
	public UniqueIDM() {
		
	}
	
	public synchronized final long getIDM() throws IOException {
	    return LocalGetIDM();
	}
	
	protected abstract long LocalGetIDM() throws IOException;
	
}