import java.nio.ByteBuffer;

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

	/**
	 * 
	 * Converti la valeur int :562 en 6 char dans un byte[]-> 000562
	 * 
	 * @param value
	 * @param numberOfBytes
	 * @return byte[numberOfBytes]=value
	 * @throws Exception
	 */
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

	public void setContenu(byte[] data) {
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
