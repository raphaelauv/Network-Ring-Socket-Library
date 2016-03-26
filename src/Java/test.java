import java.io.IOException;

public class test {
	public static void main(String[] args) {
		Thread.currentThread().setName("SOFT -APPL");
		
		Message a =new Message("XXXX 12345678".getBytes());
		//a.setId(421);
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
						premier.test(false);
					}		
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
		System.out.println("fin main");
	}
}