package Application;

import Protocol.Ringo;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.numberOfBytesException;

public class MyRunnableSend implements Runnable {
	private final ReceveSend sender;
	private Appl appl;
	
	public MyRunnableSend(Appl appl,ReceveSend sender) {
		this.appl=appl;
		this.sender = sender;
	}

	public void run(){
		boolean entrytested;
		while (appl.runContinue){
			entrytested = appl.testEntry();
			if (!entrytested) {
				try {
					sender.doSend();
				} catch (numberOfBytesException e) {
					//TODO
					System.out.println("\nERREUR SizeMessageException !! the limit is : " + Ringo.maxSizeMsg);
				} catch (DOWNmessageException e) {
					System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
					appl.runContinue = false;
				} catch (InterruptedException e) {
					appl.runContinue= false;
				}
			}
		}
		System.out.println("\nTHREAD: APP SEND   | END");
		appl.ThRecev.interrupt();
	}
}