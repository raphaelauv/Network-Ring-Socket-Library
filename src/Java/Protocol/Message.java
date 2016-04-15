package Protocol;
import Protocol.Exceptions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Stock et Parse les informations d'un message
 * 
 *
 */
public class Message {
	
	//private String type;
	private boolean multi;
	private byte[] data;
	
	private TypeMessage type;
	private String ip;
	private String ip_diff;
	private String ip_succ;
	
	private Integer port;
	private String portString;
	
	private Integer port_diff;
	private String port_diffString;
	
	private Integer port_succ;
	private String port_succString;
	
	private long idm;
	private byte [] idmLITTLE_ENDIAN_8;
	
	private String id_app;
	private byte[] data_app;
	
	private final static Integer sizeIp = Ringo.octalSizeIP;
	private final static Integer sizePort = Ringo.octalSizePort;
	private final static Integer sizeTypeMSG = Ringo.octalSizeTypeMSG;
	
	public final static int FLAG_IP_DIFF = 1;
	public final static int FLAG_IP_NORMAL = 2;
	public final static int FLAG_IP_SUCC = 2;
	
	public String getIp(){
		return this.ip;
	}
	public Integer getPort(){
		return this.port;
	}
	
	/**
	 * Create a new Message and Parse it from unknown DATA
	 * @param data
	 * @throws unknownTypeMesssage
	 * @throws parseMessageException
	 */
	public Message(byte[] data) throws unknownTypeMesssage, parseMessageException {
		super();
		this.data = data;
		try {
			this.parse();
		} catch (IndexOutOfBoundsException e) {
			throw new unknownTypeMesssage();
		}
		this.convertALL();
	}
	
	public Message(byte [] data,String NOPARSE){
		super();
		this.data=data;
	}
	
	private Message(byte[] data, TypeMessage type) {
		super();
		this.setMulti(false);
		this.data = data;
		this.type = type;
	}
	/**
	 * Convertir les chiffres dans la representation attendu par RINGO
	 * @param msg
	 */
	private void convertALL(){
		try {
			if(this.ip!=null){
				this.ip=convertIP(this.ip);
			}
			if(this.port!=null){
				this.portString=convertPort(this.port);
			}
			if(this.port_diff!=null){
				this.port_diffString=convertPort(this.port_diff);
			}
			if(this.port_succ!=null){
				this.port_succString=convertPort(this.port_succ);
			}
			if(this.ip_diff!=null){
				this.ip_diff=convertIP(this.ip_diff);
			}
			if(this.ip_succ!=null){
				this.ip_succ=convertIP(this.ip_succ);
			}
			if(this.idm!=-1){
				this.idmLITTLE_ENDIAN_8=Message.longToByteArray(this.idm,8, ByteOrder.LITTLE_ENDIAN);
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private String getDataFromNtoV(int n, int v) {
		try{
			String tmp = new String(this.data, n, v - n);
			return tmp;
		}catch(StringIndexOutOfBoundsException e){
			return "";
		}
	}
	
	/**
	 * Parcer le contenu d'un nouveau message
	 * 
	 * @throws unknownTypeMesssage
	 * @throws IndexOutOfBoundsException
	 * @throws parseMessageException
	 */
	private void parse() throws unknownTypeMesssage ,IndexOutOfBoundsException, parseMessageException{
		int curseur=0;
		int sizeIp_SPACE_PORT=sizeIp+1+sizePort;
		String strParsed=getDataFromNtoV(curseur,sizeTypeMSG);
		
		curseur=curseur+sizeTypeMSG;
		//System.out.println("type reconnu : "+strParsed);
		
		
		try{
			this.type=TypeMessage.valueOf(strParsed);	
		}catch(IllegalArgumentException e){
			throw new unknownTypeMesssage();
		}
		
		if(type==TypeMessage.DOWN){
			parseTestEnd(curseur);
			return;
		}
		if(type==TypeMessage.ACKC || type==TypeMessage.ACKD || type==TypeMessage.NOTC){
			strParsed=getDataFromNtoV(curseur,curseur+1);
			if(!strParsed.equals("\n")){
				throw new parseMessageException();
			}
			parseTestEnd(curseur+1);
			return;
		}
		
		parseTestSpace(curseur);
		
		
		if(type==TypeMessage.NEWC || type==TypeMessage.MEMB || type==TypeMessage.WELC){
			curseur++;
			parse_IP_SPACE_Port(curseur,FLAG_IP_NORMAL);
			
			curseur=curseur+sizeIp_SPACE_PORT;
			if(!(type==TypeMessage.WELC)){
				
				parseTestEnd(curseur);
				return;
					
			}
			parseTestSpace(curseur);
			curseur++;
			parse_IP_SPACE_Port(curseur,FLAG_IP_DIFF);
			curseur=curseur+sizeIp_SPACE_PORT;
			parseTestEnd(curseur);
			return;
			
		}
		
		curseur++;
		strParsed=getDataFromNtoV(curseur,curseur+Ringo.octalSizeIdm);
		this.idm=byteArrayToLong(strParsed.getBytes(),Ringo.octalSizeIdm,ByteOrder.LITTLE_ENDIAN);
		
		curseur=curseur+Ringo.octalSizeIdm;
		if(type==TypeMessage.WHOS || type==TypeMessage.EYBG){
			parseTestEnd(curseur);
			return;
		}
		parseTestSpace(curseur);
		curseur++;
		if(type==TypeMessage.TEST){
			parse_IP_SPACE_Port(curseur,FLAG_IP_DIFF);
			curseur=curseur+sizeIp_SPACE_PORT;
			parseTestEnd(curseur);
			return;
		}
		if(type==TypeMessage.APPL){
			strParsed=getDataFromNtoV(curseur,curseur+Ringo.octalSizeIdApp);
			this.id_app=strParsed;
			curseur=curseur+Ringo.octalSizeIdApp;
			parseTestSpace(curseur);
			curseur++;
			/*
			strParsed=getDataFromNtoV(curseur,curseur+Ringo.octalSizeMessSize);
			 
			curseur=curseur+Ringo.octalSizeMessSize;
			this.size_messString=strParsed;
			this.size_mess=Integer.parseInt(this.size_messString);
			parseTestSpace(curseur);
			curseur++;
			*/
			
			this.data_app= Arrays.copyOfRange(this.data, curseur, data.length);
			
			return;
		}
		
		parse_IP_SPACE_Port(curseur,FLAG_IP_NORMAL);
		curseur=curseur+sizeIp_SPACE_PORT;
		parseTestSpace(curseur);
		curseur++;
		
		if(type==TypeMessage.DUPL){
			parse_IP_SPACE_Port(curseur,FLAG_IP_DIFF);
			curseur=curseur+sizeIp_SPACE_PORT;
			parseTestEnd(curseur);
			return;
		}
		if(type==TypeMessage.GBYE){
			parse_IP_SPACE_Port(curseur,FLAG_IP_SUCC);
			curseur=curseur+sizeIp_SPACE_PORT;
			parseTestEnd(curseur);
			return;
		}	
	}
	/**
	 * Permet de parser une adrese IP puis un espace puis un port
	 * @param start position de debut
	 * @param FLAG_IP = IP_NORMAL || IP_DIFF || IP_SUCC
	 * @throws parseMessageException
	 */
	public void parse_IP_SPACE_Port(int start,int FLAG_IP) throws parseMessageException{
		
		String strParsed;
		int valEndIP= start+Ringo.octalSizeIP;
		strParsed=getDataFromNtoV(start,valEndIP);
		parseTestIp(strParsed);
		if(FLAG_IP==FLAG_IP_DIFF){
			this.ip_diff=strParsed;
		}else if(FLAG_IP==FLAG_IP_NORMAL){
			this.ip=strParsed;
		}
		else{
			this.ip_succ=strParsed;
		}
		parseTestSpace(valEndIP);
		strParsed=getDataFromNtoV(valEndIP+1,valEndIP+1+Ringo.octalSizePort);
		parseTestPort(strParsed);
		
		
		int valPort=Integer.parseInt(strParsed);
		if(FLAG_IP==FLAG_IP_DIFF){
			this.port_diff=valPort;
		}else if(FLAG_IP==FLAG_IP_NORMAL){
			this.port=valPort;
		}
		else{
			this.port_succ=valPort;
		}
	}
	
	/**
	 * Pour parse
	 * test si le message est fini
	 * @param end
	 * @throws parseMessageException souleve une erreur si message pas fini
	 */
	private void parseTestEnd(int end) throws parseMessageException{	
		if(this.data.length!=end){
				throw new parseMessageException();
			}
	}
	
	/**
	 * Pour parse
	 * test si le caractere start est un caractere d'espace
	 * @param start
	 * @throws parseMessageException souleve une erreur si ce n'est pas un espace
	 */
	private void parseTestSpace(int start) throws parseMessageException{
		if(! (new String(this.data,start,1).equals(" "))){
			throw new parseMessageException();
		}
	}
	
	/**
	 * Pour parse
	 * test si le parametre est un numero de port conventionel
	 * @param portTest
	 * @throws parseMessageException
	 */
	private void parseTestPort(String portTest)throws parseMessageException{
		if(portTest.length()!=4){
			throw new parseMessageException();
		}
		try{
			int tmp=Integer.parseInt(portTest.substring(0,4));
			if(tmp<0 || tmp>9999){
				throw new parseMessageException();
			}
		}catch(NumberFormatException e){
			throw new parseMessageException();
		}
		
		
	}
	
	/**
	 * pour parse
	 * test si le parametre est un numero d'adresse Ip conventionnel
	 * @param ipTest
	 * @throws parseMessageException
	 */
	private void parseTestIp(String ipTest) throws parseMessageException{
		if(ipTest.length()!=15){
			throw new parseMessageException();
		}
		int tmp;
		for(int i=0;i<15;i++){
			if(i==3 || i==7 || i==11){
				if(ipTest.charAt(i)!='.'){
					throw new parseMessageException();
				}
			}
			else{
				try{
					tmp=Integer.parseInt(ipTest.substring(i, i+3));
					 if(tmp<0 || tmp>255){
						 throw new parseMessageException();
					 }
					 i=i+2;
				}catch(NumberFormatException e){
					throw new parseMessageException();
				}
			}
		}
	}
	
	/**
	 * Afficher un message
	 */
	public String toString(){
		
		String str =this.type.toString();
		
		if(type==TypeMessage.DOWN){
			return str;
		}
		if(type==TypeMessage.ACKC || type==TypeMessage.ACKD || type==TypeMessage.NOTC){
			return str+"\\n";
		}
		
		if(type==TypeMessage.NEWC || type==TypeMessage.MEMB || type==TypeMessage.WELC){
			
			str=str+" "+this.ip+" "+this.portString;
			if(!(type==TypeMessage.WELC)){
				return str;
			}
			return str+" "+this.ip_diff+" "+this.port_diffString;
			
		}	
		try {
			str=str+" "+longToStringRepresentation(this.idm, 8);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		if(type==TypeMessage.WHOS || type==TypeMessage.EYBG){
			return str;
		}
		if(type==TypeMessage.TEST){
			return str+" "+this.ip_diff +" "+this.port_diffString;
		}
		if(type==TypeMessage.APPL){
			return str+" "+this.id_app+" "+new String(this.data_app);
		}
		str=str+" "+this.ip+" "+this.portString;
		if(type==TypeMessage.DUPL){
			str=str+" "+this.ip_diff+" "+this.port_diffString;
		}
		if(type==TypeMessage.GBYE){
			str=str+" "+this.ip_succ+" "+this.port_succString;
		}
		//TODO POURT TESTS
		else{
			str=new String(this.data);
		}
		return str;
	}
	
	public static Message WELC(String ip, int listenPortUDP, String ip_diff ,int port_diff) {
		
		byte[] WELC = new byte[4+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		Message tmp=new Message(WELC,TypeMessage.WELC);
		
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		tmp.convertALL();;
		tmp.remplirData("WELC ".getBytes(),tmp.ip.getBytes(),(" "+tmp.portString+" ").getBytes(),
				tmp.ip_diff.getBytes(),(" "+tmp.port_diffString).getBytes());
		return tmp;

	}
	
	
	public static Message NEWC(String ip ,int portUDP1) {
		byte[] NEWC = new byte[4+1+sizeIp+1+sizePort];
		Message tmp=new Message(NEWC,TypeMessage.NEWC);
		tmp.ip=ip;
		tmp.port=portUDP1;
		tmp.convertALL();;
		tmp.remplirData("NEWC ".getBytes(),tmp.ip.getBytes(),(" "+tmp.portString).getBytes());
		return tmp;
	}
	
	public static Message MEMB(String ip ,int portUDP1) {
		byte[] MEMB = new byte[4+1+sizeIp+1+sizePort];
		Message tmp=new Message(MEMB,TypeMessage.MEMB);
		tmp.ip=ip;
		tmp.port=portUDP1;
		tmp.convertALL();;
		tmp.remplirData("MEMB ".getBytes(),tmp.ip.getBytes(),(" "+tmp.portString).getBytes());
		return tmp;
	}
	
	public static Message GBYE(long idm, String ip, int listenPortUDP, String ip_succ, int port_succ) {
		byte[] GBYE = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		Message tmp=new Message(GBYE,TypeMessage.GBYE);
		tmp.idm=idm;
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_succ=ip_succ;
		tmp.port_succ=port_succ;
		
		tmp.convertALL();;
		tmp.remplirData("GBYE ".getBytes(),tmp.idmLITTLE_ENDIAN_8,
				(" "+tmp.ip+" "+tmp.portString+" "+tmp.ip_succ+" "+tmp.port_succString).getBytes());
		return tmp;
	}
	
	public static Message DUPL(long idm, String ip, int listenPortUDP, String ip_diff ,int port_diff) {
		byte[] DUPL = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort+1+sizeIp+1+sizePort];
		
		Message tmp=new Message(DUPL,TypeMessage.DUPL);
		tmp.idm=idm;
		tmp.ip=ip;
		tmp.port=listenPortUDP;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		tmp.convertALL();;
		tmp.remplirData("DUPL ".getBytes(),tmp.idmLITTLE_ENDIAN_8,
				(" "+tmp.ip+" "+tmp.portString+" "+tmp.ip_diff+" "+tmp.port_diffString).getBytes());
		return tmp;
	}
	public static Message EYBG(long idm) {
		byte[] EYBG = new byte[5+Ringo.octalSizeIdm];
		Message tmp=new Message(EYBG,TypeMessage.EYBG);
		tmp.idm=idm;
		tmp.convertALL();;
		tmp.remplirData("EYBG ".getBytes(),tmp.idmLITTLE_ENDIAN_8);
		return tmp;
	}
	public static Message WHOS(long idm) {
		byte[] WHOS = new byte[4+1+Ringo.octalSizeIdm];//[5+8]=13

		Message tmp=new Message(WHOS,TypeMessage.WHOS);
		tmp.idm=idm;
		tmp.convertALL();;
		tmp.remplirData("WHOS ".getBytes(),tmp.idmLITTLE_ENDIAN_8);
		return tmp;
	}
	
	public static Message APPL(long idm , String id_app, byte[] data_app) {
		byte[] APPL = new byte[4+1+Ringo.octalSizeIdm+1+8+1+data_app.length];
		Message tmp=new Message(APPL,TypeMessage.APPL);
		tmp.idm=idm;
		tmp.id_app=id_app;
		tmp.data_app=data_app;
		tmp.convertALL();;
		tmp.remplirData("APPL ".getBytes(),tmp.idmLITTLE_ENDIAN_8,(" "+tmp.id_app+" ").getBytes(),tmp.data_app);
		return tmp;
	}
	
	public static Message TEST(long idm, String ip_diff ,int port_diff) {
		byte[] TEST = new byte[4+1+Ringo.octalSizeIdm+1+sizeIp+1+sizePort];
		
		Message tmp=new Message(TEST,TypeMessage.TEST);
		tmp.idm=idm;
		tmp.ip_diff=ip_diff;
		tmp.port_diff=port_diff;
		tmp.convertALL();;
		tmp.remplirData("TEST ".getBytes(),tmp.idmLITTLE_ENDIAN_8,
				(" "+tmp.ip_diff+" ").getBytes(),tmp.port_diffString.getBytes());
		return tmp;
	}
	
	public static Message ACKC() {
		byte[] ACKC = new String("ACKC\n").getBytes();
		Message tmp = new Message(ACKC, TypeMessage.ACKC);
		return tmp;
	}

	public static Message ACKD() {
		byte[] ACKD = new String("ACKD\n").getBytes();
		Message tmp = new Message(ACKD, TypeMessage.ACKD);
		return tmp;
	}
	
	public static Message DOWN() {
		byte[] DOWN = new String("DOWN").getBytes();
		Message tmp = new Message(DOWN, TypeMessage.DOWN);
		tmp.multi = true;
		return tmp;
	}

	public static Message NOTC() {
		byte[] NOTC = new String("NOTC\n").getBytes();
		Message tmp = new Message(NOTC, TypeMessage.NOTC);
		return tmp;
	}
	
	/**
	 * Rempli this.data avec les args
	 * @param args
	 */
	private void remplirData(byte[]... args) {
		int i = 0;
		for (byte[] arg1 : args) {
			for (byte arg2 : arg1) {
				this.data[i] = arg2;
				i++;
			}
		}
	}
	
	/**
	 * 
	 * Crée un String de la valeur 562 sur 6 -> 000562
	 * 
	 * @param value
	 * @param numberOfBytes
	 * @return 
	 * @throws Exception
	 */
	public static String longToStringRepresentation(long value,int numberOfBytes) throws numberOfBytesException{
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
		return tmp;
	}
	
	public static byte[] longToByteArray(Long val,int numberOfByte,ByteOrder ENDIAN){
		if(val<0){		
		}
		//long values = Long.parseUnsignedLong("18446744073709551615");
		return ByteBuffer.allocate(numberOfByte).order(ENDIAN).putLong(val).array();
	}
	
	public static Long byteArrayToLong(byte[] bytes ,int numberOfByte,ByteOrder ENDIAN){
		ByteBuffer buffer = ByteBuffer.allocate(numberOfByte).order(ENDIAN);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getLong();
	}
	
	/**
	 * Convertir un port 45 -> 0045
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private String convertPort(int port) throws Exception{
		
		int size=(""+port).length();
		if(size>4 || port<0){
			throw new Exception();
		}
		int diff=4-size;
		String result=(""+port);
		for(int i=0;i<diff;i++){
			result="0"+result;
		}
		return result;
	}
	
	/**
	 * Convertir une ip 192.0.0.1 -> 192.000.000.001
	 * @param ip
	 * @return 
	 * @throws Exception
	 */
	private static String convertIP(String ip) throws Exception{
		
		if(ip=="localhost"){
			//TODO pour TEST
			return ip;
		}
		String[]tmp=ip.split("\\.");
		
		if(tmp.length!=4){
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
		
		//TODO pour test , a retirer
		if(tmp2.length()!=15){
			throw new Exception();
		}
		return tmp2;
	}
	
	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
	public byte[] getData() {
		return data;
	}
	public TypeMessage getType() {
		return type;
	}
	public String getIp_diff() {
		return ip_diff;
	}
	public Integer getPort_diff() {
		return port_diff;
	}
	public long getIdm() {
		return idm;
	}
	public String getId_app() {
		return id_app;
	}
	public byte[] getData_app() {
		return data_app;
	}
}