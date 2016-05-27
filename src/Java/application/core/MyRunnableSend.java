package application.core;

import java.io.IOException;

import protocol.Ringo;
import protocol.exceptions.*;

public class MyRunnableSend implements Runnable {
	
	private final ApplSendReceve appl;
	
	public MyRunnableSend(ApplSendReceve sender) {
		this.appl=sender;
		
	}

	public void run(){
		if(appl.modeService){
			return;
		}
		while (appl.runContinue){
			String input = appl.testEntry();
			if (input!=null) {
				try {
					appl.send(input);
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