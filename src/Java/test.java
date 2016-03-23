import java.io.IOException;

public class test {
	public static void main(String[] args) {
		Serv premier;
		try {
			premier = new Serv(true);
			
			while (true) {
					Thread.sleep(4000);
					//System.out.println(premier.lire());	
					try {
						premier.envoyer("mamama", 10);
					} catch (SizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
