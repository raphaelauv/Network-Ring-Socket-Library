package protocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import protocol.exceptions.RingoSocketCloseException;

class ServMULTI {
	
	String ip_diff;
	Integer port_diff;
	
	String ip_diff2;
	Integer port_diff2;
	private SelectionKey selKey1;
	private SelectionKey selKey2;
	private MembershipKey key1;
	private MembershipKey key2;
	private DatagramChannel dc1;
	private DatagramChannel dc2;
	Selector sel;
	
	Object registeringSync = new Object(); //mutex contre deadlock avec wakeup
	private RingoSocket ringoSocket;
	Runnable runServMULTI;
	boolean erreur;
	
	ServMULTI(RingoSocket ringoSocket, String ip_diff ,int port_diff) throws IOException{
		this.ringoSocket=ringoSocket;
		this.ip_diff=ip_diff;
		this.port_diff=port_diff;
		this.erreur = false;
		this.sel = Selector.open();
		this.updateMulti();
		
		this.runServMULTI = new Runnable() {
			public void run() {
				while (!erreur) {
					try {
						receveMULTI();
					} catch (RingoSocketCloseException | IOException | InterruptedException |ClosedSelectorException e) {
						erreur = true;
						ringoSocket.boolClose=true;
						System.out.println("FERMETURE MULTI");
					}
				}
				ringoSocket.printVerbose("END");
			}
		};
	}
	
	public void updateMulti() throws IOException{
		
		/*
		if(ringoSocket.sockMultiRECEP!=null){
			ringoSocket.sockMultiRECEP.close();
		}
		
		ringoSocket.sockMultiRECEP = new MulticastSocket(this.port_diff);
		ringoSocket.sockMultiRECEP.joinGroup(InetAddress.getByName(this.ip_diff));
		*/
		
		if (this.dc1 != null) {
			this.dc1.close();
			this.selKey1.cancel();
			this.dc1 = null;
		}
		add(this.ip_diff, this.port_diff);
	}
	
	
	private void testMultiMsg(InetSocketAddress isa, SelectionKey sk, DatagramChannel dc, ByteBuffer byteBuffer)
			throws IOException {

		String b = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		ringoSocket.printVerbose("message MULTI RECEVE : " + b);

		if (b.startsWith("DOWN")) {
			if (!ringoSocket.isDUPL) {
				ringoSocket.closeRingoSocket(true);
			} else {
				dc.close();
				sk.cancel();
				ringoSocket.isDUPL = false;
			}
		}

	}
	
	private void receveMULTI()
			throws IOException, RingoSocketCloseException, InterruptedException, ClosedSelectorException {
		/*
		 * byte[] data = new byte[Ringo.maxSizeMsg]; DatagramPacket paquet = new
		 * DatagramPacket(data, data.length);
		 * 
		 * ringoSocket.sockMultiRECEP.receive(paquet);
		 * 
		 * String st = new String(paquet.getData(), 0, paquet.getLength());
		 * ringoSocket.printVerbose("message MULTI RECEVE : " + st);
		 * 
		 * if (st.startsWith("DOWN")) { ringoSocket.closeRingoSocket(true); }
		 * 
		 */

		ByteBuffer byteBuffer = ByteBuffer.allocate(512);
		int val = sel.select();
		synchronized (registeringSync) {

			Iterator<SelectionKey> it = sel.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey sk = it.next();
				byteBuffer.clear();
				if (key1 != null && key1.isValid() && sk.isReadable() && sk.channel() == dc1) {
					InetSocketAddress isa = (InetSocketAddress) this.dc1.receive(byteBuffer);
					byteBuffer.flip();
					testMultiMsg(isa, sk, dc1, byteBuffer);

				} else if (key2 != null && key2.isValid() && sk.isReadable() && sk.channel() == dc2) {
					InetSocketAddress isa = (InetSocketAddress) this.dc2.receive(byteBuffer);
					byteBuffer.flip();
					testMultiMsg(isa, sk, dc2, byteBuffer);
				}
			}

		}

	}
	
	
	public void add(String ip_diff,int port_diff) throws IOException{
		MembershipKey keyTMP;
		DatagramChannel dcTMP;
		SelectionKey selKeyTMP = null;
		
		InetAddress group = InetAddress.getByName(ip_diff);

		/*******************
		 * LifeHack
		 */
	    MulticastSocket sockMultiRECEP = new MulticastSocket(port_diff);
	    sockMultiRECEP.joinGroup(group);
	    NetworkInterface ni1=sockMultiRECEP.getNetworkInterface();
	    sockMultiRECEP.leaveGroup(group);
	    sockMultiRECEP.close();
	    //*******************

	    dcTMP= DatagramChannel.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(port_diff))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni1);
	    
		keyTMP=dcTMP.join(group, ni1);
		dcTMP.configureBlocking(false);
		
		synchronized (registeringSync) {
			 this.sel.wakeup();
			 selKeyTMP =dcTMP.register(this.sel, SelectionKey.OP_READ);
		 }
		
		if(this.dc1==null){
			this.key1=keyTMP;
			this.dc1=dcTMP;
			this.selKey1=selKeyTMP;
		}
		else if(this.dc2==null){
			this.key2=keyTMP;
			this.dc2=dcTMP;
			this.selKey2=selKeyTMP;
		}
		else{
		
		}
		
		
	}
	
}
