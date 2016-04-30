package protocol;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
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
	
	private MembershipKey key1;
	private MembershipKey key2;
	private DatagramChannel dc1;
	private DatagramChannel dc2;
	Selector sel;
	
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
		
		add(this.ip_diff, this.port_diff);
	}
	
	
	private void testMultiMsg(ByteBuffer byteBuffer) throws IOException{
		
		String b = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		ringoSocket.printVerbose("message MULTI RECEVE : " + b);

		if (b.startsWith("DOWN")) {
			ringoSocket.closeRingoSocket(true);
		}
	}
	
	private void receveMULTI() throws IOException, RingoSocketCloseException, InterruptedException,ClosedSelectorException {
		/*
		byte[] data = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(data, data.length);
		
		ringoSocket.sockMultiRECEP.receive(paquet);
		
		String st = new String(paquet.getData(), 0, paquet.getLength());
		ringoSocket.printVerbose("message MULTI RECEVE : " + st);

		if (st.startsWith("DOWN")) {
			ringoSocket.closeRingoSocket(true);
		}
		
		*/
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(512);
		sel.select();
		Iterator<SelectionKey> it = sel.selectedKeys().iterator();
		while (it.hasNext()) {
			SelectionKey sk = it.next();
			byteBuffer.clear();
			if (key1 != null &&key1.isValid() && sk.isReadable() && sk.channel() == dc1) {
				this.dc1.receive(byteBuffer);
				byteBuffer.flip();
				testMultiMsg(byteBuffer);
			}
			else if (key2 != null && key2.isValid() && sk.isReadable() && sk.channel() == dc2) {
				this.dc2.receive(byteBuffer);
				byteBuffer.flip();
				testMultiMsg(byteBuffer);
			}
		}
		
	}
	
	
	public void add(String ip_diff,int port_diff) throws IOException{
		MembershipKey keyTMP;
		DatagramChannel dcTMP;
		
		InetAddress group = InetAddress.getByName(ip_diff);

	    MulticastSocket sockMultiRECEP = new MulticastSocket(port_diff);
	    sockMultiRECEP.joinGroup(group);
	    NetworkInterface ni1=sockMultiRECEP.getNetworkInterface();
	    sockMultiRECEP.leaveGroup(group);
	    sockMultiRECEP.close();
		
	    dcTMP= DatagramChannel.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(port_diff))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni1);
		
		keyTMP=dcTMP.join(group, ni1);
		
		dcTMP.configureBlocking(false);
		
		dcTMP.register(this.sel, SelectionKey.OP_READ);
		
		
		if(this.key1==null){
			this.key1=keyTMP;
			this.dc1=dcTMP;
		}
		else if(this.key2==null){
			this.key2=keyTMP;
			this.dc2=dcTMP;
		}
		else{
			//TODO
		}
	}
	
}
