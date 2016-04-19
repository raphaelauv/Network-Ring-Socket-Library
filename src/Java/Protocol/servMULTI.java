package Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import Protocol.Exceptions.DOWNmessageException;

class servMULTI {
	
	private RingoSocket ringoSocket;
	Runnable runServMULTI;
	
	servMULTI(RingoSocket ringoSocket) {
		this.ringoSocket=ringoSocket;
		this.runServMULTI = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMULTI();
					} catch (DOWNmessageException | IOException | InterruptedException e) {
						erreur = true;
					}
				}
				ringoSocket.printVerbose("END");
			}
		};
	}
	
	private void receveMULTI() throws IOException, DOWNmessageException, InterruptedException {
		ringoSocket.sockMultiRECEP = new MulticastSocket(ringoSocket.port_diff);
		ringoSocket.sockMultiRECEP.joinGroup(InetAddress.getByName(ringoSocket.ip_diff.toString()));

		byte[] data = new byte[100];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		ringoSocket.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		ringoSocket.printVerbose("message MULTI RECEVE : " + st);

		if (st.equals("DOWN\n")) {
			ringoSocket.closeServ(true);
			throw new DOWNmessageException(); // to the thread MULTI
		}

	}

}
