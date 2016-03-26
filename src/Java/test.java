import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class test {
	public static void main(String[] args) throws UnknownHostException, IpException {
		Thread.currentThread().setName("SOFT -APPL");
		
		Message a =new Message("XXXX 12345678".getBytes());
		
		
		try {
			Message.convertIP("192.0.0.1");
			Message.intToCharBytes(100, 8);
			byte[]bytes=Message.intToByteArrayLittle_indian_8(0,8,ByteOrder.LITTLE_ENDIAN);
			
			for (byte b : bytes) {
				   System.out.format("0x%x ", b);
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//a.setId(421);
		/*
		System.out.println(new String(a.getData()));
		
		try {
			System.out.println("arg0 : "+args[0]); //4242
			System.out.println("arg1 : "+args[1]); //5555

			Serv premier = new Serv(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
			premier.verboseMode=true;
			
					Thread.sleep(4000);
					//System.out.println(premier.lire());	
					for(int i=0 ; i<10 ;i++){
						premier.send(new String("mamama"+i).getBytes());
						
					}
					//premier.test(false);
					premier.close();
					premier.close();
			
		} catch (DOWNmessageException e) {
			System.out.println("DOWN recu dans main");
		} catch (SizeMessageException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		System.out.println("fin main");
	}
}