package application.core;

import protocol.exceptions.*;
import java.io.IOException;

public class MyRunnableReceve implements Runnable {
	private final ReceveSend recever;
	private final Appl appl;
	
	public MyRunnableReceve(ReceveSend recever) {
		this.appl=(Appl)recever;
		this.recever = recever;
	}

	public void run() {
		while (appl.runContinue) {
			try {
				recever.doReceve(appl.ringoSocket.receive());

			} catch (RingoSocketCloseException e) {
				appl.printModeApplication("the socket is CLOSE");
				appl.runContinue = false;
			} catch (InterruptedException e) {
				appl.runContinue = false;
			} catch (IOException e) {
				appl.printModeApplication("THREAD: APP RECEVE | File error");
			} catch (NumberOfBytesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				appl.printModeApplication("the MSG is incorrect");
				appl.runContinue= false;
			}
		}
		if(appl.verboseMode){
			appl.printModeApplication("THREAD: APP RECEVE | END");
		}
		appl.ThSend.interrupt();
	}
}
