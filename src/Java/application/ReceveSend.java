package application;

import java.io.IOException;
import protocol.Message;
import protocol.exceptions.RingoSocketCloseException;
import protocol.exceptions.NumberOfBytesException;
import protocol.exceptions.ParseException;

public interface ReceveSend {
	
	public void doReceve(Message msg) throws RingoSocketCloseException, IOException, NumberOfBytesException, InterruptedException, ParseException;
	
	
	
	public void doSend(String input) throws NumberOfBytesException, RingoSocketCloseException, InterruptedException, ParseException;
	
	/**
	 * Mode service
	 * @return content d'un message recu
	 * @throws InterruptedException
	 */
	public byte[] output() throws Exception, InterruptedException;
	
	
	/**
	 * Mode service , demande fermeture du service
	 * @throws RingoSocketCloseException
	 * @throws Exception
	 */
	public void close() throws RingoSocketCloseException ,Exception;
}