package protocol;
import protocol.exceptions.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

class ServTCP {

	private RingoSocket ringoSocket;
	Runnable runServTcp;
	
	ServTCP(RingoSocket ringoSocket) {
		this.ringoSocket=ringoSocket;
		this.runServTcp= new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						serv();
					} catch (ProtocolException | IOException | InterruptedException e) {
						try {
							ringoSocket.testClose();
						} catch (DOWNmessageException e1) {
							erreur = true;
						}
					}
				}
				ringoSocket.printVerbose("END");
			}
		};
	}
		
	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * 
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws InterruptedException 
	 * @throws DOWNmessageException
	 */
	private void serv() throws IOException, ProtocolException, InterruptedException {

			Socket socket = ringoSocket.sockServerTCP.accept();
			ringoSocket.tcpAcces.acquire();
			ringoSocket.printVerbose("TCP connect");
			

			BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());
			Message msg1;
			if(ringoSocket.isDUPL){
				msg1=Message.NOTC();
			}else{
				msg1 = Message.WELC(ringoSocket.ip, ringoSocket.portUDP1, ringoSocket.ip_diff, ringoSocket.port_diff);
			}
			buffOut.write(msg1.getData());
			buffOut.flush();

			if(ringoSocket.isDUPL){
				buffOut.close();
				buffIn.close();
				ringoSocket.tcpAcces.release();
				return;
			}
			ringoSocket.printVerbose("TCP : message SEND   : " + msg1.toString());

			byte[] tmp = new byte[Ringo.maxSizeMsg];
			int sizeReturn = buffIn.read(tmp);
			/*//TODO
			if (sizeReturn != 26) {
				throw new ProtocolException();
			}
			tmp = Arrays.copyOfRange(tmp, 0, 25);
			 */
			Message msg2 = null;
			try {
				msg2 = Message.parseMessage(tmp);
			} catch (ParseMessageException | UnknownTypeMesssage e) {
				ringoSocket.printVerbose("TCP : erreur protocol");
				return;
			}

			Message msg3;
			if (msg2.getType() == TypeMessage.NEWC) {
				msg3 = Message.ACKC();
			}else if(msg2.getType() == TypeMessage.DUPL){
				msg3 = Message.ACKD(ringoSocket.listenPortUDP);
			}
			else{
				throw new ProtocolException();
			}
			ringoSocket.printVerbose("TCP : message RECEVE : " + msg2.toString());

			buffOut.write(msg3.getData());
			buffOut.flush();
			
			ringoSocket.printVerbose("TCP : message SEND   : " + msg3.toString());

			ringoSocket.UDP_ipPort_Acces.acquire();			
			ringoSocket.portUDP1 = msg2.getPort();
			ringoSocket.ipPortUDP1=msg2.getIp();
			ringoSocket.UDP_ipPort_Acces.release();
			buffOut.close();
			buffIn.close();
			
			ringoSocket.tcpAcces.release();
		}	
}