package uidm;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Arrays;

import protocol.Message;
import protocol.Ringo;

public class IpPort_IDM extends UniqueIDM{

	int port;
	String ip;
	
	private byte [] idmStart=new byte[8];
	private Long idmActuel;
	public IpPort_IDM(String ip,int port) throws UnknownHostException{
		this.idmActuel=1L;
		this.build_IDM_array();
	}
	
	/**
	 * Build the constant start of the IDM array
	 * 
	 * @throws UnknownHostException
	 */
	private void build_IDM_array() throws UnknownHostException{

		InetAddress ip = InetAddress.getByName(this.ip);
		byte[] ipBytes = ip.getAddress();
		
		byte[] portBytes = new byte[2];
		portBytes[0] = (byte)(this.port & 0xFF);
		portBytes[1] = (byte)((this.port >> 8) & 0xFF);
		
		this.idmStart= new byte[Ringo.byteSizeIdm];
		
		int cmp=0;
		for(int j=0;j<ipBytes.length;j++){
			this.idmStart[cmp]=ipBytes[j];
			cmp++;
		}
		for(int j=0; j<portBytes.length;j++){
			this.idmStart[cmp]=portBytes[j];
			cmp++;
		}
	}
	
	@Override
	protected long LocalGetIDM() throws IOException {
		byte [] end_of_IDM= new byte[2];	
		this.idmActuel=this.idmActuel%65000;//~limite de 2^255;
		end_of_IDM[0] = (byte)(this.idmActuel & 0xFF);
		end_of_IDM[1] = (byte)((this.idmActuel >> 8) & 0xFF);
		idmActuel++;
		byte[] val=Arrays.copyOf(this.idmStart, Ringo.byteSizeIdm);		
		val[6]=end_of_IDM[0];
		val[7]=end_of_IDM[1];
		return Message.byteArrayToLong(val,Ringo.byteSizeIdm, ByteOrder.nativeOrder());
	
	}
}
