package application;

import java.io.IOException;

import protocol.Message;
import protocol.exceptions.DOWNmessageException;
import protocol.exceptions.NumberOfBytesException;

public interface ReceveSend {
	
	void doReceve(Message msg) throws DOWNmessageException, IOException, NumberOfBytesException, InterruptedException;
	void doSend() throws NumberOfBytesException, DOWNmessageException, InterruptedException;
}