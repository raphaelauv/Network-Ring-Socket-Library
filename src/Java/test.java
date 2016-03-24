import java.io.IOException;

public class test {
	public static void main(String[] args) {
		Serv premier;
		try {
			System.out.println("arg0 : "+args[0]); //4242
			System.out.println("arg1 : "+args[1]); //5555
			premier = new Serv(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
			premier.verboseMode=true;
			
			while (true) {
					Thread.sleep(4000);
					//System.out.println(premier.lire());	
					
					premier.envoyer("mamama", 10);
					
			}
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOWNmessageException e) {
			System.out.println("DOWN recu dans main");
		} catch (SizeException e) {
			e.printStackTrace();
		}
		System.out.println("fin main");
	}
}
