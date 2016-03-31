import java.nio.ByteBuffer;
import java.nio.ByteOrder;
class IpException extends Exception{
}
class numberOfBytesException extends Exception{
}
public class Message {
	
	private String type;
	private boolean multi;
	private byte[] data;
	
	private String ip;
	private String ip_diff;
	private String ip_succ;
	
	private Integer port;
	private Integer port_diff;
	private Integer port_succ;
	
	private long idm;
	private String idmS;
	
	private String id_app;
	private byte[] message_app;
	
	public static Integer sizeIp=Ringo.octalSizeIP;
	public static Integer sizePort=Ringo.octalSizePort;
	
	
	public Message(byte[] data,String type) {
		super();
		this.setMulti(false);
		this.data = data;
		this.type=type;
	}
	
	
	private static void convertALL(Message msg){
		try {
			if(msg.ip!=null){
				msg.ip=convertIP(msg.ip);
			}
			if(msg.ip_diff!=null){
				msg.ip_diff=convertIP(msg.ip_diff);
			}
			if(msg.ip_succ!=null){
				msg.ip_succ=convertIP(msg.ip_succ);
			}
			if(msg.idm!=0){
				msg.idmS=longToCharBytes(msg.idm, 8);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString(){
		
		String str =this.type;
		
		if(type=="DOWN"){
			return str;
		}
		if(type=="ACKC" || type=="ACKD" || type=="NOTC"){
			return str+"\\n";
		}
		
		if(type=="NEWC" || type=="MEMB"){
			return str+" "+this.ip+" "+this.port;
		}
	
		if(type=="WELC"){
			return str+" "+this.ip+" "+this.port+" "+this.ip_diff+" "+this.port_diff;
		}
		
		str=str+" "+idmS;
		
		if(type=="WHOS" || type=="EYBG"){
			return str;
		}
		if(type=="TEST"){
			return str+" "+this.ip_diff +" "+this.port_diff;
		}
		if(type=="APPL"){
			return str+" "+this.id_app+" "+new String(this.message_app);
		}
		
		str=str+" "+this.ip+" "+this.port;
		if(type=="DUPL"){
			str=str+" "+this.ip_diff+" "+this.port_diff;
		}
		if(type=="GBYE"){
			str=str+" "+this.ip_succ+" "+this.port_succ;
		}
		//TODO POURT TESTS
		else{
			str=new String(this.data);
		}
		return str;
	}
	
	public static Message WELC(String ip, int listenPortUDP, String ip_diff ,int port_diff) {
		
		byte[] WELC = new byte[4+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort+1];//4+1+8+1+4+1+8+1+4+1 = 33
		Message tmp=new Message(WELC,"WELC");
		
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		convertALL(tmp);
		return tmp;

	}
	
	public static Message NEWC(String ip ,int portUDP1) {
		byte[] NEWC = new byte[4+1+sizeIp+sizePort+1];
		Message tmp=new Message(NEWC,"NEWC");
		tmp.ip=ip;
		tmp.port=portUDP1;
		convertALL(tmp);
		return tmp;
	}
	
	public static Message MEMB(String ip ,int portUDP1) {
		byte[] MEMB = new byte[4+1+sizeIp+1+sizePort];
		Message tmp=new Message(MEMB,"MEMB");
		tmp.ip=ip;
		tmp.port=portUDP1;
		convertALL(tmp);
		return tmp;
	}
	
	public static Message GBYE(long idm, String ip, int listenPortUDP, String ip_succ, int port_succ) {
		byte[] GBYE = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		Message tmp=new Message(GBYE,"GBYE");
		tmp.idm=idm;
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_succ=ip_succ;
		tmp.port_succ=port_succ;
		
		convertALL(tmp);
		return tmp;
	}
	
	public static Message DUPL(long idm, String ip, int listenPortUDP, String ip_diff ,int port_diff) {
		byte[] DUPL = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		
		Message tmp=new Message(DUPL,"DUPL");
		tmp.idm=idm;
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		convertALL(tmp);
		return tmp;
	}
	public static Message EYBG(long idm) {
		byte[] EYBG = new byte[5+Ringo.octalSizeIdm];
		Message tmp=new Message(EYBG,"EYBG");
		tmp.idm=idm;
		convertALL(tmp);
		return tmp;
	}
	public static Message WHOS(long idm) {
		byte[] WHOS = new byte[4+1+Ringo.octalSizeIdm];//[5+8]=13

		Message tmp=new Message(WHOS,"WHOS");
		tmp.idm=idm;
		convertALL(tmp);
		return tmp;
	}
	
	public static Message APPL(long idm , String id_app , byte[] message_app) {
		byte[] APPL = new byte[Ringo.maxSizeMsg];
		Message tmp=new Message(APPL,"APPL");
		tmp.idm=idm;
		tmp.id_app=id_app;
		convertALL(tmp);
		return tmp;
	}
	
	public static Message TEST(long idm, String ip_diff ,int port_diff) {
		byte[] TEST = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort];

		Message tmp=new Message(TEST,"TEST");
		tmp.idm=idm;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		convertALL(tmp);
		return tmp;
	}
	
	public static Message ACKC() {
		byte[] ACKC = new byte[4+1];
		ACKC=new String("ACKC\n").getBytes();
		Message tmp=new Message(ACKC,"ACKC");
		convertALL(tmp);
		return tmp;
	}
	public static Message ACKD() {
		
		byte[] ACKD = new byte[4+1];
		ACKD=new String("ACKD\n").getBytes();
		Message tmp=new Message(ACKD,"ACKD");
		convertALL(tmp);
		return tmp;
	}
	
	public static Message DOWN() {
		byte[] DOWN = new byte[4];
		DOWN=new String("DOWN").getBytes();
		Message tmp=new Message(DOWN,"DOWN");
		tmp.multi=true;
		convertALL(tmp);
		return tmp;
	}
	public static Message NOTC() {
		byte[] NOTC = new byte[4+1];
		NOTC=new String("NOTC\n").getBytes();

		Message tmp=new Message(NOTC,"NOTC");
		convertALL(tmp);
		return tmp;
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
	public static String longToCharBytes(long value,int numberOfBytes) throws Exception{
		if(value<0){
			throw new numberOfBytesException();
		}
		int numberOfZERO = numberOfBytes - (Long.toString(value)).length();
		if(numberOfZERO<0){
			throw new numberOfBytesException();
		}
		String tmp="";
		for(int i=0;i<numberOfZERO;i++){
			tmp=tmp+"0";
		}
		tmp=tmp+value;
		
		System.out.println(tmp);
		if(tmp.length()!=numberOfBytes){
			System.out.println("pas bonne taille");
			throw new Exception();
		}
		return tmp;
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
	public static String convertIP(String ip) throws Exception{
		
		if(ip=="localhost"){
			//TODO pour TEST
			return ip;
		}
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
		
		//TODO pour test
		if(tmp2.length()!=15){
			
			System.out.println("pas 15");
			throw new Exception();
		}
		return tmp2;
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
