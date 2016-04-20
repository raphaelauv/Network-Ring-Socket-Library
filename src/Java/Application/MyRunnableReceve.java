package application;
import protocol.exceptions.*;
import java.io.IOException;

public class MyRunnableReceve implements Runnable {
	private final ReceveSend recever;
	private Appl appl;
	
	public MyRunnableReceve(Appl appl,ReceveSend recever) {
		this.appl=appl;
		this.recever = recever;
	}

	public void run() {
		while (appl.runContinue) {
			try {
				recever.doReceve(appl.ringoSocket.receive());

			} catch (DOWNmessageException e) {
				System.out.println("THREAD: APP RECEVE | DOWNmessageException");
				appl.runContinue = false;
			} catch (InterruptedException e) {
				appl.runContinue = false;
			} catch (IOException e) {
				System.out.println("THREAD: APP RECEVE | File error");
			} catch (NumberOfBytesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\nTHREAD: APP RECEVE | END");
		appl.ThSend.interrupt();
	}
}
