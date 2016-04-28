package protocol;
import protocol.exceptions.*;
import protocol.TypeMessage;
import java.io.IOException;
import java.net.DatagramPacket;

class ServUDPlisten {
	private RingoSocket ringoSocket;
	Runnable runServUDPlisten;
	
	public ServUDPlisten(RingoSocket ringoSocket) {
		this.ringoSocket = ringoSocket;
		this.runServUDPlisten=new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMessage();
					} catch (IOException | InterruptedException | RingoSocketCloseException | ParseException e) {
						erreur = true;
						ringoSocket.boolClose=true;
					}
				}
				ringoSocket.printVerbose("END");
			}
		};	
	}
	
	private void receveMessage() throws IOException, InterruptedException, RingoSocketCloseException, ParseException {

		byte[] dataToReceve = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);

		ringoSocket.sockRecever.receive(paquet);// attente passive
		Message msgR = null;
		try {
			msgR = Message.parseMessage(paquet.getData());
		} catch (ParseException | UnknownTypeMesssage e) {
			ringoSocket.printVerbose("MESSAGE INCORRECT : "+new String(paquet.getData()));
			return;
		}
		if (msgR.getType() == TypeMessage.TEST) {
			synchronized (ringoSocket.TESTisComeBack) {
				if (msgR.getIdm() == ringoSocket.ValTEST) {
					ringoSocket.TESTisComeBackBool = true;
					ringoSocket.TESTisComeBack.notify();
					return;
				}
				else{
					if(msgR.getIp_diff()!=ringoSocket.ip_diff || 
							msgR.getPort_diff() != ringoSocket.port_diff){
						return;// si le message n'est pas pour cet anneau , pas renvoyer
					}
				}
			}
		}
		if (ringoSocket.IdAlreadyReceveUDP1.contains(msgR.getIdm())) {
			ringoSocket.printVerbose("Message DEJA ENVOYER OU RECU : " + msgR.getIdm());
			return;
		} else {
			ringoSocket.IdAlreadyReceveUDP1.add(msgR.getIdm());
		}
		ringoSocket.printVerbose("Message Recu    : " + msgR.toString());

		if (msgR.getType() == TypeMessage.GBYE) {
			if(msgR.getIp().equals(ringoSocket.ipPortUDP1) && msgR.getPort().equals(ringoSocket.portUDP1)){
				ringoSocket.printVerbose("My next leave the RING");
				ringoSocket.send(Message.EYBG(this.ringoSocket.getUniqueIdm()));
				
				ringoSocket.EYBG_Acces.acquire(); //pour attendre que EYBG soit bien envoyer
				
				ringoSocket.UDP_ipPort_Acces.acquire();
				ringoSocket.ipPortUDP1=msgR.getIp_succ();
				ringoSocket.portUDP1=msgR.getPort_succ();
				ringoSocket.UDP_ipPort_Acces.release();
				return;
			}
		}
		else if (msgR.getType() == TypeMessage.APPL) {
			if (msgR.getId_app().equals(ringoSocket.idApp)) {
				synchronized (ringoSocket.listForApply) {
					ringoSocket.listForApply.add(msgR);
					ringoSocket.listForApply.notifyAll();
				}
				return;
			}

		}else if (msgR.getType() == TypeMessage.WHOS) {
			ringoSocket.send(Message.MEMB(this.ringoSocket.getUniqueIdm(),ringoSocket.idApp,ringoSocket.ip, ringoSocket.portUDP1));	
			
		} else if (msgR.getType() == TypeMessage.EYBG) {
			synchronized (ringoSocket.EYBGisArrive) {
				ringoSocket.EYBGisArriveBool=true;
				ringoSocket.EYBGisArrive.notify();
			}
			return;
		}
		synchronized (ringoSocket.listToSend) {
			ringoSocket.listToSend.add(msgR);
			ringoSocket.listToSend.notify();
		}
	}
}