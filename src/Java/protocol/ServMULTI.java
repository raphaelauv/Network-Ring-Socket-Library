package protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import protocol.exceptions.DOWNmessageException;

class ServMULTI {
	
	private RingoSocket ringoSocket;
	Runnable runServMULTI;
	
	ServMULTI(RingoSocket ringoSocket) {
		this.ringoSocket=ringoSocket;
		try {
			this.updateMulti();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
	
	public void updateMulti() throws IOException{
		
		ringoSocket.sockMultiRECEP = new MulticastSocket(ringoSocket.port_diff);
		ringoSocket.sockMultiRECEP.joinGroup(InetAddress.getByName(ringoSocket.ip_diff.toString()));
	}
	
	private void receveMULTI() throws IOException, DOWNmessageException, InterruptedException {

		byte[] data = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		ringoSocket.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		ringoSocket.printVerbose("message MULTI RECEVE : " + st);

		if (st.startsWith("DOWN")) {
			ringoSocket.closeServ(true);
		}
	}
}
