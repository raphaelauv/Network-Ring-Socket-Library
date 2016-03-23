import java.io.IOException;

public class test {
	public static void main(String[] args) {
		Serv premier;
		try {
			premier = new Serv(true,5555,4242);
			
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
