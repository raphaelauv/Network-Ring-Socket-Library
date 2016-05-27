package application.core;

import protocol.exceptions.*;
import java.io.IOException;

public class MyRunnableReceve implements Runnable {
	
	private final ApplSendReceve recever;
	
	public MyRunnableReceve(ApplSendReceve recever) {
		this.recever=recever;
	}

	public void run() {
		while (recever.runContinue) {
			try {
				recever.doReceve(recever.ringoSocket.receive());

			} catch (RingoSocketCloseException e) {
				recever.printModeApplication("the socket is CLOSE");
				recever.runContinue = false;
			} catch (InterruptedException e) {
				recever.runContinue = false;
			} catch (IOException e) {
				recever.printModeApplication("THREAD: APP RECEVE | File error");
			} catch (NumberOfBytesException e) {
				//TODO
				e.printStackTrace();
			} catch (ParseException e) {
				recever.printModeApplication("the MSG is incorrect");
				recever.runContinue= false;
			}
		}
		if(recever.verboseMode){
			recever.printModeApplication("THREAD: APP RECEVE | END");
		}
		recever.ThSend.interrupt();
	}
}
