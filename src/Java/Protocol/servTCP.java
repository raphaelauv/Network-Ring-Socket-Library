package Protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.ProtocolException;
import Protocol.Exceptions.parseMessageException;
import Protocol.Exceptions.unknownTypeMesssage;

class servTCP {

	private RingoSocket ringoSocket;
	Runnable runServTcp;
	
	servTCP(RingoSocket ringoSocket) {
		this.ringoSocket=ringoSocket;
		this.runServTcp= new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						serv();
					} catch (ProtocolException | IOException e) {
						try {
							ringoSocket.isClose();
						} catch (DOWNmessageException e1) {
							erreur = true;
						}
					}
				}
				ringoSocket.printVerbose("END thread TCP");
			}
		};
	}
		
	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * 
	 * @param idTCP
	 *            port TCP of serv
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws DOWNmessageException
	 */
	private void serv() throws IOException, ProtocolException {
		
			
			Socket socket = ringoSocket.sockServerTCP.accept();
			try {
				ringoSocket.tcpAcces.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();//TODO
			}
			ringoSocket.printVerbose("TCP connect");

			BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());

			Message msg1 = Message.WELC(ringoSocket.ip, ringoSocket.portUDP1, ringoSocket.ip_diff, ringoSocket.port_diff);
			buffOut.write(msg1.getData());
			buffOut.flush();

			ringoSocket.printVerbose("TCP : message SEND   : " + msg1.toString());

			byte[] tmp = new byte[Ringo.maxSizeMsg];
			int sizeReturn = buffIn.read(tmp);
			
			if (sizeReturn != 25) {
				throw new ProtocolException();
			}
			//tmp = Arrays.copyOfRange(tmp, 0, 25);

			Message msg2 = null;
			try {
				msg2 = new Message(tmp);
			} catch (parseMessageException | unknownTypeMesssage e) {
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
			
			try {
				ringoSocket.UDP_ipPort_Acces.acquire();
				System.out.println("verroux acquis");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			ringoSocket.portUDP1 = msg2.getPort();
			ringoSocket.ipPortUDP1=msg2.getIp();
			
			ringoSocket.UDP_ipPort_Acces.release();
			System.out.println("verroux liberer");
			buffOut.close();
			buffIn.close();
			
			ringoSocket.tcpAcces.release();
		}	
}