package protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import protocol.RingoSocket.EntityInfo;
import protocol.exceptions.RingoSocketCloseException;

class ServMULTI {
	
	MultiChanel multiRing;
	LinkedList<MultiChanel> listMultiChannel;
	
	class MultiChanel{
		EntityInfo entityinfo;
		SelectionKey selKey;
		MembershipKey key;
		DatagramChannel dc;
		public MultiChanel(EntityInfo entityinfo, SelectionKey selKey, MembershipKey key,
				DatagramChannel dc) {
			this.entityinfo=entityinfo;
			this.selKey = selKey;
			this.key = key;
			this.dc = dc;
		}
	}
	
	Selector sel;
	
	Object registeringSync = new Object(); //mutex contre deadlock avec wakeup
	private RingoSocket ringoSocket;
	Runnable runServMULTI;
	boolean erreur;
	
	ServMULTI(RingoSocket ringoSocket,EntityInfo entityinfo) throws IOException{
		this.ringoSocket=ringoSocket;
		this.listMultiChannel =new LinkedList<MultiChanel>();
		
		this.erreur = false;
		this.sel = Selector.open();
		this.multiRing=addMultiDiff(entityinfo);
		
		this.runServMULTI = new Runnable() {
			public void run() {
				while (!erreur) {
					try {
						ringoSocket.testClose();
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
	
	MultiChanel addMultiDiff(EntityInfo entityinfo) throws IOException{
		
		MembershipKey keyTMP;
		DatagramChannel dcTMP;
		SelectionKey selKeyTMP = null;		
		InetAddress group = InetAddress.getByName(entityinfo.ip_diff);

		/*******************
		 * LifeHack
		 */
	    MulticastSocket sockMultiRECEP = new MulticastSocket(entityinfo.port_diff);
	    sockMultiRECEP.joinGroup(group);
	    NetworkInterface ni1=sockMultiRECEP.getNetworkInterface();
	    sockMultiRECEP.leaveGroup(group);
	    sockMultiRECEP.close();
	    //*******************

	    dcTMP= DatagramChannel.open(StandardProtocolFamily.INET)
				.setOption(StandardSocketOptions.SO_REUSEADDR, true)
				.bind(new InetSocketAddress(entityinfo.port_diff))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni1);
	    
		keyTMP=dcTMP.join(group, ni1);
		dcTMP.configureBlocking(false);
		MultiChanel tmp =  new MultiChanel(entityinfo, selKeyTMP, keyTMP, dcTMP);
		this.listMultiChannel.add(tmp);

		synchronized (registeringSync) {
			 this.sel.wakeup();
			 tmp.selKey =tmp.dc.register(this.sel, SelectionKey.OP_READ);
		 }
		return tmp;	
	}
	
	private void removeMulti(String ip_diff,Integer port_diff) throws IOException{
		MultiChanel todelete = null;
		for(MultiChanel mc : this.listMultiChannel){
			if(mc.entityinfo.ip_diff.equals(ip_diff) && mc.entityinfo.port_diff==port_diff){
				todelete=mc;
			}
		}
		if(todelete!=null){
			todelete.dc.close();
			todelete.selKey.cancel();
			todelete.selKey=null; //TODO
			this.listMultiChannel.remove(todelete);
		}
	}
	
	/**
	 * Update la soket de multidiff principale , celle de l'anneau
	 * @param ip_diff
	 * @param port_diff
	 * @throws IOException
	 */
	public void updateMulti(EntityInfo entityinfo) throws IOException{
		
		/*
		if(ringoSocket.sockMultiRECEP!=null){
			ringoSocket.sockMultiRECEP.close();
		}
		
		ringoSocket.sockMultiRECEP = new MulticastSocket(this.port_diff);
		ringoSocket.sockMultiRECEP.joinGroup(InetAddress.getByName(this.ip_diff));
		*/
		
		/*
		if(this.multiRing.entityinfo.ip_diff.equals(entityinfo.ip_diff) && this.multiRing.entityinfo.port_diff==entityinfo.port_diff){
			//meme ip et port de diff
			return;
		}
		*/
		removeMulti(entityinfo.ip_diff, entityinfo.port_diff);
		addMultiDiff(entityinfo);
	}
	
	private void testMultiMsg(InetSocketAddress isa, SelectionKey sk, MultiChanel mc, ByteBuffer byteBuffer)
			throws IOException {

		String b = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		ringoSocket.printVerbose("message MULTI RECEVE : " + b);
		if (b.startsWith("DOWN")) {
			if (!ringoSocket.isDUPL) {
				ringoSocket.closeRingoSocket(true);
			} else {
				System.out.println("dupl a non dupl");
				mc.dc.close();
				//sk.cancel();
				ringoSocket.isDUPL = false;
				
				if(this.multiRing==mc){
					this.multiRing=this.listMultiChannel.getFirst();
				}
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
		sel.select();
		synchronized (registeringSync) {

			Iterator<SelectionKey> it = sel.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey sk = it.next();
				byteBuffer.clear();
				for(MultiChanel mc : this.listMultiChannel){
					try{
						if (mc.key != null && mc.key.isValid() && sk.isReadable() && sk.channel() == mc.dc) {
							InetSocketAddress isa = (InetSocketAddress) mc.dc.receive(byteBuffer);
							byteBuffer.flip();
							testMultiMsg(isa, sk, mc, byteBuffer);
						}
					}
					catch(CancelledKeyException e){
						System.out.println("CancelledKeyException");
					}
				}
			}
		}
	}
	
}
