package protocol;
import protocol.exceptions.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

class ServTCP implements Runnable{

	private RingoSocket ringoSocket;

	ServTCP(RingoSocket ringoSocket) {
		this.ringoSocket=ringoSocket;
	}
	
	public void run(){
		boolean erreur = false;
		while (!erreur) {
			try {
				serv();
			} catch (IOException | InterruptedException | ParseException e) {
				
			} catch (RingoSocketCloseException e) {
				erreur = true;
				ringoSocket.boolClose.set(true);;
			}
		}
		try {ringoSocket.sockServerTCP.close();} catch (IOException e) {}
		ringoSocket.printVerbose("END");
		ringoSocket.boolClose.set(true);;
	}
	
	
		
	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * 
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws InterruptedException 
	 * @throws ParseException 
	 * @throws RingoSocketCloseException
	 */
	private void serv() throws IOException, InterruptedException, ParseException, RingoSocketCloseException {

		ringoSocket.testClose();
		Socket socket = ringoSocket.sockServerTCP.accept();
		ringoSocket.tcpAcces.acquire();
		byte[] tmpMsg2 = new byte[Ringo.maxSizeMsg];
		try {

			ringoSocket.printVerbose("TCP connect");
			BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());

			Message msg1;
			if (ringoSocket.isDUPL.get()) {
				msg1 = Message.NOTC();
			} else {
				msg1 = Message.WELC(ringoSocket.ip, ringoSocket.principal.portUdp, ringoSocket.principal.ip_diff,
						ringoSocket.principal.port_diff);
			}

			buffOut.write(msg1.getData());
			buffOut.flush();

			if (ringoSocket.isDUPL.get()) {
				buffOut.close();
				buffIn.close();
				return;
			}

			ringoSocket.printVerbose("TCP : message SEND   : " + msg1.toString());

			int sizeReturn = buffIn.read(tmpMsg2);
			/*
			 * //TODO if (sizeReturn != 26) { throw new ProtocolException(); }
			 * tmp = Arrays.copyOfRange(tmp, 0, 25);
			 */
			Message msg2;
			Message msg3;

			msg2 = Message.parseMessage(tmpMsg2);

			boolean modeDUPL = false;

			if (msg2.getType() == TypeMessage.NEWC) {
				msg3 = Message.ACKC();
			} else if (msg2.getType() == TypeMessage.DUPL) {
				modeDUPL = true;
				msg3 = Message.ACKD(ringoSocket.listenPortUDP);
			} else {
				throw new UnknownTypeMesssage();
			}
			ringoSocket.printVerbose("TCP : message RECEVE : " + msg2.toString());

			buffOut.write(msg3.getData());
			buffOut.flush();

			ringoSocket.printVerbose("TCP : message SEND   : " + msg3.toString());
			ringoSocket.UDP_MULTI_ipPort_Acces.acquire();
			if (modeDUPL) {

				ringoSocket.secondaire = ringoSocket.new EntityInfo(msg2.getIp(), msg2.getPort(), msg2.getIp_diff(),
						msg2.getPort_diff());
				ringoSocket.isDUPL.set(true);
				ringoSocket.servMulti.addMultiDiff(ringoSocket.secondaire);
			} else {
				ringoSocket.principal.ipUdp = msg2.getIp();
				ringoSocket.principal.portUdp = msg2.getPort();
			}
			ringoSocket.UDP_MULTI_ipPort_Acces.release();
			buffOut.close();
			buffIn.close();

		} catch (IOException | InterruptedException e) {
			ringoSocket.tcpAcces.release();
			throw e;
		} catch (ParseException | UnknownTypeMesssage e) {
			ringoSocket.printVerbose("TCP : erreur de protocol\n    message recu : " + new String(tmpMsg2));
		}
		ringoSocket.boolDisconnect.set(false);
		ringoSocket.tcpAcces.release();

	}
}