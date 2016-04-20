package Application;

import java.io.IOException;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.numberOfBytesException;

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
				appl.msgIN = appl.ringoSocket.receive();
				byte[] msgInByte = appl.msgIN.getData_app();
				recever.doReceve(msgInByte);

			} catch (DOWNmessageException e) {
				System.out.println("THREAD: APP RECEVE | DOWNmessageException");
				appl.runContinue = false;
			} catch (InterruptedException e) {
				appl.runContinue = false;
			} catch (IOException e) {
				System.out.println("THREAD: APP RECEVE | File error");
			} catch (numberOfBytesException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\nTHREAD: APP RECEVE | END");
		appl.ThSend.interrupt();
	}
}
