package protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import protocol.exceptions.RingoSocketCloseException;

class ServMULTI {
	
	private RingoSocket ringoSocket;
	Runnable runServMULTI;
	boolean erreur;
	
	ServMULTI(RingoSocket ringoSocket) throws IOException{
		this.ringoSocket=ringoSocket;
		this.erreur = false;
		this.updateMulti();
		this.runServMULTI = new Runnable() {
			public void run() {
				while (!erreur) {
					try {
						receveMULTI();
					} catch (RingoSocketCloseException | IOException | InterruptedException e) {
						erreur = true;
						ringoSocket.boolClose=true;
					}
				}
				ringoSocket.printVerbose("END");
			}
		};
	}
	
	public void updateMulti() throws IOException{
		if(ringoSocket.sockMultiRECEP!=null){
			ringoSocket.sockMultiRECEP.close();
		}
		ringoSocket.sockMultiRECEP = new MulticastSocket(ringoSocket.port_diff);
		ringoSocket.sockMultiRECEP.joinGroup(InetAddress.getByName(ringoSocket.ip_diff.toString()));
	}
	
	private void receveMULTI() throws IOException, RingoSocketCloseException, InterruptedException {

		byte[] data = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		ringoSocket.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		ringoSocket.printVerbose("message MULTI RECEVE : " + st);

		if (st.startsWith("DOWN")) {
			ringoSocket.closeRingoSocket(true);
		}
	}
}
