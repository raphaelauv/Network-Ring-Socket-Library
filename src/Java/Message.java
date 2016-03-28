import java.nio.ByteBuffer;
import java.nio.ByteOrder;
class IpException extends Exception{
}
class numberOfBytesException extends Exception{
}
public class Message {
	
	private boolean multi; 
	private byte[] data;
	
	private String ip;
	private String ip_diff;
	private String ip_succ;
	
	private int port;
	private int port_diff;
	private int port_succ;
	
	private byte[] idm;
	
	private byte[] id_app;
	private byte[] id;
	
	private int size_mess;
	private int size_nom;
	
	private byte[] num_mess;
	private byte[] no_mess;
	
	private int size_content;

	public static int sizeIp=Ringo.octalSizeIP;
	public static int sizePort=Ringo.octalSizePort;
	
	public Message(byte[] data) {
		super();
		this.setMulti(false);
		this.data = data;
	}
	
	
	/**
	 * 
	 * Converti la valeur int :562 en 6 char dans un byte[]-> 000562
	 * 
	 * @param value
	 * @param numberOfBytes
	 * @return byte[numberOfBytes]=value
	 * @throws Exception
	 */
	
	public static Message WELC(String ip, int listenPortUDP, String ip_diff ,int port_diff) {

		byte[] WELC = new byte[4+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort+1];//4+1+8+1+4+1+8+1+4+1 = 33
		Message tmp=new Message(WELC);
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;

		return tmp;

	}
	
	public static Message NEWC(String ip ,int portUDP1) {
		byte[] NEWC = new byte[4+1+sizeIp+sizePort+1];
		Message tmp=new Message(NEWC);
		tmp.ip=ip;
		tmp.port=portUDP1;
		return tmp;
	}
	
	public static Message MEMB(String ip ,int portUDP1) {
		byte[] MEMB = new byte[4+1+sizeIp+1+sizePort];
		Message tmp=new Message(MEMB);
		tmp.ip=ip;
		tmp.port=portUDP1;
		return tmp;
	}
	
	public static Message GBYE(int idm, String ip, int listenPortUDP, String ip_succ, int port_succ) {
		byte[] GBYE = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		Message tmp=new Message(GBYE);
		//tmp.idm=idm;
		tmp.ip_succ=ip_succ;
		tmp.port_succ=port_succ;
		return tmp;
	}
	
	public static Message DUPL(int idm, String ip, int listenPortUDP, String ip_diff ,int port_diff) {
		byte[] DUPL = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		
		Message tmp=new Message(DUPL);
		//TODO  idm
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;

		return tmp;
	}
	public static Message EYBG(int idm) {
		byte[] EYBG = new byte[5+Ringo.octalSizeIdm];
		Message tmp=new Message(EYBG);
		return tmp;
	}
	public static Message WHOS(int idm) {
		byte[] WHOS = new byte[4+1+Ringo.octalSizeIdm];//[5+8]=13

		Message tmp=new Message(WHOS);
		return tmp;
	}
	
	public static Message APPL(int idm , int id_app , byte[] message_app) {
		byte[] APPL = new byte[Ringo.maxSizeMsg];
		Message tmp=new Message(APPL);
		return tmp;
	}
	
	public static Message TEST(int idm, String ip_diff ,int port_diff) {
		byte[] TEST = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort];

		Message tmp=new Message(TEST);

		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		return tmp;
	}
	
	public static Message ACKC() {
		byte[] ACKC = new byte[4+1];
		ACKC=new String("ACKC\n").getBytes();
		Message tmp=new Message(ACKC);
		return tmp;
	}
	public static Message ACKD() {
		
		byte[] ACKD = new byte[4+1];
		ACKD=new String("ACKD\n").getBytes();
		Message tmp=new Message(ACKD);
		return tmp;
	}
	
	public static Message DOWN() {
		byte[] DOWN = new byte[4];
		DOWN=new String("DOWN").getBytes();
		Message tmp=new Message(DOWN);
		return tmp;
	}
	public static Message NOTC() {
		byte[] NOTC = new byte[4+1];
		NOTC=new String("NOTC\n").getBytes();

		Message tmp=new Message(NOTC);
		return tmp;
	}
	
	
	public static byte[] intToCharBytes(int value,int numberOfBytes) throws Exception{
		if(value<0){
			throw new numberOfBytesException();
		}
		int numberOfZERO = numberOfBytes - Integer.toString(value).length();
		if(numberOfZERO<0){
			throw new numberOfBytesException();
		}
		String tmp="";
		for(int i=0;i<numberOfZERO;i++){
			tmp=tmp+"0";
		}
		tmp=tmp+Integer.toString(value);
		
		System.out.println(tmp);
		if(tmp.length()!=numberOfBytes){
			System.out.println("pas bonne taille");
			throw new Exception();
		}
		return tmp.getBytes();
	}
	
	/**
	 * 
	 * 
	 * @param value Unsigned Long : max  2^64-1
	 * @param numberOfByte nombre de byte sur lequel est stocker la valeur
	 * @param ENDIAN 
	 * @return
	 */
	public static byte[] intToByteArrayLittle_indian_8(UnsignedLong val,int numberOfByte,ByteOrder ENDIAN){
		if(val.getValue()<0){
			
		}
		//long values = Long.parseUnsignedLong("18446744073709551615");
		    return ByteBuffer.allocate(numberOfByte).order(ENDIAN).putLong(val.getValue()).array();
		}
	
	/**
	 * Converti une ip 192.0.0.1 -> 192.000.000.001
	 * @param ip
	 * @return byte[15]
	 * @throws Exception
	 */
	public static byte[] convertIP(String ip) throws Exception{
		
		String[]tmp=ip.split("\\.");
		
		if(tmp.length!=4){
			System.out.println("pas 4");
			throw new IpException();
		}
		//to put the 000
		for(int i=0; i<4;i++){
			if(tmp[i].length()==1){
				tmp[i]="00"+tmp[i];
			}
			else if(tmp[i].length()==2){
				tmp[i]="0"+tmp[i];
			}
		}
		String tmp2=tmp[0]+"."+tmp[1]+"."+tmp[2]+"."+tmp[3];
		if(tmp2.length()!=15){
			System.out.println("pas 15");
			throw new Exception();
		}
		return tmp2.getBytes();
	}
	

	public byte[] getData() {
		return data;
	}
	public byte[] getDataForApply() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}


	public int getId() {
		return Integer.parseInt (new String(data,4,9));
	}

	public String toString(){
		return new String(this.data);
	}

	public void setId(Integer id) {
	
		int numbOfZERO=8-(id.toString().length());
		
		byte[] zero = ByteBuffer.allocate(10).putInt(0).array();
		
		byte[] byteId = ByteBuffer.allocate(10).putInt(id).array();
	
		int conteur=0;
		for(int i=5 ;i<13;i++){
			if(numbOfZERO>0){
				data[i]=zero[0];
				numbOfZERO--;
			}
			else{
				data[i]=byteId[conteur];
				conteur++;
			}
		}
	}

	public boolean isMulti() {
		return multi;
	}

	public void setMulti(boolean multi) {
		this.multi = multi;
	}

}
