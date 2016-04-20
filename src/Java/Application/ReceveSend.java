package Application;

import java.io.IOException;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.numberOfBytesException;

public interface ReceveSend {
	
	void doReceve(byte[] msgInByte) throws DOWNmessageException, IOException, numberOfBytesException, InterruptedException;
	void doSend() throws numberOfBytesException, DOWNmessageException, InterruptedException;
}
