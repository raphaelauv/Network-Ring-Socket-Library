package protocol;
import protocol.exceptions.*;
import protocol.TypeMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

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
						ringoSocket.testClose();
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
				
				if (ringoSocket.ValTest!=null && ringoSocket.ValTest.equals(msgR.getIdm())) {
					ringoSocket.TESTisComeBackBool = true;
					ringoSocket.TESTisComeBack.notify();
					return;
				}
				else{
					//TODO comparaison a verifier avec future sturture
					if( !(  msgR.getIp_diff().equals(ringoSocket.principal.ip_diff) && 
						msgR.getPort_diff().equals(ringoSocket.principal.port_diff ))){
						ringoSocket.printVerbose("message test pas pour cet anneau");
						return;// si le message n'est pas pour cet anneau , pas renvoyer
					}
				}
			}
		}
		
		if (msgR.getType() == TypeMessage.WHOS) {
			synchronized (ringoSocket.TESTisComeBack) {
				if(ringoSocket.ValTest!=null && ringoSocket.ValTest.equals(msgR.getIdm())){
						ringoSocket.TESTisComeBackBool = true;
						ringoSocket.TESTisComeBack.notify();
						return;
				}
				else{
					ringoSocket.send(Message.MEMB(this.ringoSocket.getUniqueIdm(),ringoSocket.idApp,ringoSocket.ip, ringoSocket.listenPortUDP));
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
			if(msgR.getIp().equals(ringoSocket.principal.ipUdp) && msgR.getPort().equals(ringoSocket.principal.portUdp)){
				ringoSocket.printVerbose("My next leave the RING");
				ringoSocket.send(Message.EYBG(this.ringoSocket.getUniqueIdm()));
				ringoSocket.EYBG_Acces.acquire(); //pour attendre que EYBG soit bien envoyer
				
				ringoSocket.UDP_MULTI_ipPort_Acces.acquire();
				ringoSocket.principal.ipUdp=msgR.getIp_succ();
				ringoSocket.principal.portUdp=msgR.getPort_succ();
				
				if(ringoSocket.principal.ipUdp.equals(ringoSocket.ip) && ringoSocket.principal.portUdp.equals(ringoSocket.listenPortUDP) ){
					if(!ringoSocket.isDUPL){
						ringoSocket.printVerbose("I'm now alone , i'm DISCONNECT");
						ringoSocket.boolDisconnect=true;
					}
					//TODO
				}
				ringoSocket.UDP_MULTI_ipPort_Acces.release();
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

		}else if (msgR.getType() == TypeMessage.MEMB) {
			if(ringoSocket.ValTest!=null && ringoSocket.members!=null){
				ringoSocket.members.put(InetSocketAddress.createUnresolved(msgR.getIp(), msgR.getPort()), msgR.getId());
			}
			
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