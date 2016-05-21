package application.core;

import java.io.IOException;

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
		if(appl.modeService){
			return;
		}
		while (appl.runContinue){
			String input = appl.testEntry();
			if (input!=null) {
				try {
					sender.doSend(input);
				} catch (NumberOfBytesException e) {
					//TODO	
					appl.printModeApplication("\nERREUR SizeMessageException !! the limit is : " + Ringo.maxSizeMsg);
				} catch (RingoSocketCloseException e) {
					appl.printModeApplication("the socket is CLOSE");
					appl.runContinue = false;
				} catch (InterruptedException e) {
					appl.runContinue= false;
				} catch (ParseException e) {
					appl.printModeApplication("the MSG is incorrect");
					appl.runContinue= false;
				} catch (IOException e) {
					
				}
			}
		}
		if(appl.verboseMode){
			appl.printModeApplication("\nTHREAD: APP SEND   | END");
		}
		appl.ThRecev.interrupt();
	}
}