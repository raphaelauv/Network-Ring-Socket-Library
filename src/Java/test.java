import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class test {
	public static void main(String[] args) throws UnknownHostException, IpException {
		Thread.currentThread().setName("SOFT -APPL");
		
		
		try {
			System.out.println(Message.convertIP("192.1.1.1"));
			//Message.longToCharBytes(100, 8);
			
			new UnsignedLong("1234567891223123");
			
			byte[] a= Message.longToByteArrayLittle_indian_8(123456789122312L,8,ByteOrder.LITTLE_ENDIAN);
			
			for (byte b : a) {
				   System.out.format("0x%x ", b);
			}
			Long re=Message.byteArray_Little_indian_8ToLongToLong(a,8,ByteOrder.LITTLE_ENDIAN);
			System.out.println(re);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		if(args.length==0|| args[0]==null || args[1]==null){
			System.out.println("ATTENTION ILMANQUE ARGUMENT");
			return ;
		}
		
		try {
			System.out.println("arg0 : "+args[0]); //4242
			System.out.println("arg1 : "+args[1]); //5555

			RingoSocket premier = new RingoSocket(Integer.parseInt(args[0]),Integer.parseInt(args[1]),true);
			while(true){
				Message a=new Message("ACKC".getBytes());
				premier.receive(a);
			}
					//System.out.println(premier.lire());	
				
					/*
					for(int i=0 ; i<10 ;i++){
						premier.send(new Message(new String("mamama"+i).getBytes(),"noType"));
					}
					*/
					//premier.test(false);
					//premier.close();
					//premier.close();
			
		} catch (DOWNmessageException e) {
			System.out.println("DOWN recu dans main");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (unknownTypeMesssage e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("fin main");
	}
}