package Application;

import java.io.IOException;

import Protocol.Message;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.numberOfBytesException;

public interface ReceveSend {
	
	void doReceve(Message msg) throws DOWNmessageException, IOException, numberOfBytesException, InterruptedException;
	void doSend() throws numberOfBytesException, DOWNmessageException, InterruptedException;
}