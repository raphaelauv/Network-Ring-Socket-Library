package protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

class ServUDPsend {
	private RingoSocket ringoSocket;
	Runnable runServUDPsend;
	private String str;
	private Message msg;

	private DatagramPacket paquetMulti;
	private DatagramPacket paquet1;
	private DatagramPacket paquet2;
	private byte[] dataTosend;
	
	public ServUDPsend(RingoSocket ringoSocket) {
		this.ringoSocket = ringoSocket;
		this.runServUDPsend = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						sendMessage();
					} catch (InterruptedException | IOException e) {
						erreur = true;
						ringoSocket.boolClose=true;
					}
				}
				ringoSocket.printVerbose("END");
			}
		};
	}

	private void sendMessage() throws IOException, InterruptedException {

		synchronized (ringoSocket.listToSend) {
			while (ringoSocket.listToSend.isEmpty()) {
				ringoSocket.listToSend.notifyAll(); // pour le wait de closeServ
				ringoSocket.listToSend.wait();
			}
			this.msg = ringoSocket.listToSend.pop();
		}
		this.dataTosend = msg.getData();

		if (msg.isMulti()) {
			this.paquetMulti = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName(ringoSocket.ip_diff.toString()), ringoSocket.port_diff);
			
			ringoSocket.printVerbose("Message Envoyer DIFF : "+ msg.toString());
			ringoSocket.sockSender.send(paquetMulti);
			
			return;
		} else {
			ringoSocket.printVerbose("Message Envoyer : "+ msg.toString());
			
			this.paquet1 = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName(ringoSocket.ipPortUDP1), ringoSocket.portUDP1);
			ringoSocket.sockSender.send(paquet1);
			
			if (ringoSocket.isDUPL){
				this.paquet2 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(ringoSocket.ipPortUDP2), ringoSocket.portUDP2);
				ringoSocket.sockSender.send(paquet2);
			}
		}
		
		// Pour debloquer l'attente de changement de port
		if (this.msg.getType() == TypeMessage.EYBG) {
			ringoSocket.EYBG_Acces.release();
		}
		

	}
}