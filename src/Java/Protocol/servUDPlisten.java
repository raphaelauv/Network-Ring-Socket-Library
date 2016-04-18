package Protocol;

import Protocol.TypeMessage;

import java.io.IOException;
import java.net.DatagramPacket;

import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.parseMessageException;
import Protocol.Exceptions.unknownTypeMesssage;

class servUDPlisten {
	private RingoSocket ringoSocket;
	Runnable runServUDPlisten;
	
	public servUDPlisten(RingoSocket ringoSocket) {
		this.ringoSocket = ringoSocket;
		this.runServUDPlisten=new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMessage();
					} catch (IOException | InterruptedException | DOWNmessageException e) {
						erreur = true;
					}
				}
				ringoSocket.printVerbose("END thread RECEV");
			}
		};
		
	}
	
	private void receveMessage() throws IOException, InterruptedException, DOWNmessageException {

		byte[] dataToReceve = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);

		ringoSocket.sockRecever.receive(paquet);// attente passive
		Message msgR = null;
		try {
			msgR = new Message(paquet.getData());
		} catch (parseMessageException | unknownTypeMesssage e) {
			e.printStackTrace();//TODO
			return;
		}

		if (ringoSocket.IdAlreadyReceveUDP1.contains(msgR.getIdm())) {
			ringoSocket.printVerbose("Message DEJA ENVOYER OU RECU : " + msgR.toString());
			return;
		} else {
			ringoSocket.IdAlreadyReceveUDP1.add(msgR.getIdm());
		}
		ringoSocket.printVerbose("Message Recu    : " + msgR.toString());

		if (msgR.getType() == TypeMessage.GBYE) {
			if(msgR.getIp().equals(ringoSocket.ipPortUDP1) && msgR.getPort().equals(ringoSocket.portUDP1)){
				ringoSocket.printVerbose("My next leave the RING");
				ringoSocket.send(Message.EYBG(300));
				try{
				ringoSocket.EYBG_Acces.acquire();
				System.out.println("verroux acquis");
				}catch (InterruptedException e){
					e.printStackTrace();//TODO
				}
				ringoSocket.ipPortUDP1=msgR.getIp_succ();
				ringoSocket.portUDP1=msgR.getPort_succ();
				
				return;
			}
			
		} else if (msgR.getType() == TypeMessage.TEST) {
			if (msgR.getIdm() == ringoSocket.ValTEST) {
				ringoSocket.TESTisComeBack = true;
				return;
			}
		}else if (msgR.getType() == TypeMessage.WHOS) {
			ringoSocket.send(Message.MEMB(400,ringoSocket.idApp,ringoSocket.ip, ringoSocket.portUDP1));	
			
		} else if (msgR.getType() == TypeMessage.EYBG) {
			synchronized (ringoSocket.EYBGisArrive) {
				ringoSocket.EYBGisArriveBool=true;
				ringoSocket.EYBGisArrive.notify();
			}
			return;

		} else if (msgR.getType() == TypeMessage.APPL) {

			if (msgR.getId_app().equals(ringoSocket.idApp)) {
				synchronized (ringoSocket.listForApply) {
					ringoSocket.listForApply.add(msgR);
					ringoSocket.listForApply.notifyAll();
				}
				// TODO renvoyer automatique ou pas
			}
		}
		synchronized (ringoSocket.listToSend) {
			ringoSocket.listToSend.add(msgR);
			ringoSocket.listToSend.notifyAll();
		}
	}
	

}
