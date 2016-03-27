import java.nio.ByteBuffer;
import java.nio.ByteOrder;
class IpException extends Exception{
}
class numberOfBytesException extends Exception{
}
public class Message {
	
	private boolean multi; 
	private byte[] data;
	
	private byte[] ip;
	private byte[] ip_diff;
	private byte[] ip_succ;
	
	private byte[] port;
	private byte[] port_diff;
	private byte[] port_succ;
	
	private byte[] idm;
	
	private byte[] id_app;
	private byte[] id;
	
	private byte[] size_mess;
	private byte[] size_nom;
	
	private byte[] num_mess;
	private byte[] no_mess;
	
	private byte[] size_content;

	public static int sizeIp=Ringo.octalSizeIP;
	public static int sizePort=Ringo.octalSizePort;
	/**
	 * 
	 * Converti la valeur int :562 en 6 char dans un byte[]-> 000562
	 * 
	 * @param value
	 * @param numberOfBytes
	 * @return byte[numberOfBytes]=value
	 * @throws Exception
	 */
	
	public static byte[] WELC(String ip, int numberLICENPortUDP, String ip_diff ,int port_diff) {

		byte[] WELC = new byte[4+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort+1];//4+1+8+1+4+1+8+1+4+1 = 33
		return WELC;

	}
	
	public static byte[] NEWC(String ip ,int numberPortUDP1) {
		byte[] NEWC = new byte[4+1+sizeIp+sizePort+1];

		return NEWC;
	}
	
	public static byte[] MEMB(String ip ,int numberPortUDP1) {
		byte[] MEMB = new byte[4+1+sizeIp+1+sizePort];

		return MEMB;
	}
	
	public static byte[] GBYE(int idm, String ip, int numberLICENPortUDP, String ip_succ, int port_succ) {
		byte[] GBYE = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];

		return GBYE;
	}
	
	public static byte[] DUPL(int idm, String ip, int numberLICENPortUDP, String ip_diff ,int port_diff) {
		byte[] DUPL = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];

		return DUPL;
	}
	public static byte[] EYBG() {
		byte[] EYBG = new byte[5+Ringo.octalSizeIdm];

		return EYBG;
	}
	public static byte[] WHOS(int idm) {
		byte[] WHOS = new byte[4+1+Ringo.octalSizeIdm];//[5+8]=13

		return WHOS;
	}
	
	public static byte[] APPL(int idm , int id_app , byte[] message_app) {
		byte[] APPL = new byte[Ringo.maxSizeMsg];

		return APPL;
	}
	
	public static byte[] TEST(int idm, String ip_diff ,int port_diff) {
		byte[] TEST = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort];

		return TEST;
	}
	
	public static byte[] ACKC() {
		byte[] ACKC = new byte[4+1];

		return ACKC;
	}
	public static byte[] ACKD() {
		byte[] ACKD = new byte[4+1];

		return ACKD;
	}
	
	public static byte[] DOWN() {
		byte[] DOWN = new byte[4];

		return DOWN;
	}
	public static byte[] NOTC() {
		byte[] NOTC = new byte[4+1];

		return NOTC;
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
	
	public Message(byte[] data) {
		super();
		this.setMulti(false);
		this.data = data;
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
