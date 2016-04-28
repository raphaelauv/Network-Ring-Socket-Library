package application;

import protocol.Ringo;
import protocol.exceptions.*;

public class MyRunnableSend implements Runnable {
	private final ReceveSend sender;
	private final Appl appl;
	
	public MyRunnableSend(ReceveSend sender) {
		this.appl=(Appl)sender;
		this.sender = sender;
	}

	public void run(){
		boolean entrytested;
		while (appl.runContinue){
			entrytested = appl.testEntry();
			if (!entrytested) {
				try {
					sender.doSend();
				} catch (NumberOfBytesException e) {
					//TODO	
					appl.printModeApplication("\nERREUR SizeMessageException !! the limit is : " + Ringo.maxSizeMsg);
				} catch (DOWNmessageException e) {
					appl.printModeApplication("the socket is CLOSE");
					appl.runContinue = false;
				} catch (InterruptedException e) {
					appl.runContinue= false;
				} catch (ParseMessageException e) {
					appl.printModeApplication("the MSG is incorrect");
					appl.runContinue= false;
				}
			}
		}
		if(appl.verboseMode){
			appl.printModeApplication("\nTHREAD: APP SEND   | END");
		}
		appl.ThRecev.interrupt();
	}
}