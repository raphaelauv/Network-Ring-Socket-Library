import java.net.SocketException;

public class test {
	public static void main(String[] args) {
		
		try {
			Serv premier = new Serv();
		} catch (SocketException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
