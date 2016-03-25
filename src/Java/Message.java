import java.nio.ByteBuffer;

public class Message {
	
	private boolean multi; 
	private byte[] data;
	
	public Message(String msg){
		
	}
	public Message(byte[] data) {
		super();
		this.setMulti(false);
		this.data = data;
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
