package application;

import java.io.IOException;
import protocol.Message;
import protocol.exceptions.DOWNmessageException;
import protocol.exceptions.NumberOfBytesException;
import protocol.exceptions.ParseException;

public interface ReceveSend {
	
	public void doReceve(Message msg) throws DOWNmessageException, IOException, NumberOfBytesException, InterruptedException, ParseException;
	public void doSend() throws NumberOfBytesException, DOWNmessageException, InterruptedException, ParseException;
	
	/**
	 * Mode service
	 * @param content d'un message a envoyer
	 */
	public void input(byte [] content) throws Exception;
	
	/**
	 * Mode service
	 * @return content d'un message recu
	 * @throws InterruptedException
	 */
	public byte[] output() throws Exception, InterruptedException;
	
	
	/**
	 * Mode service , demande fermeture du service
	 * @throws DOWNmessageException
	 * @throws Exception
	 */
	public void close() throws DOWNmessageException ,Exception;
}