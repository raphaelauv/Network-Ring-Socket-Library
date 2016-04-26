
import protocol.RingoSocket;
public class Toto {

	public static void main(String[] args) throws Exception {
		RingoSocket ringo1 = new RingoSocket("DIFF####", 4444, 5555, false, false,true);
		
		RingoSocket ringo2 = new RingoSocket("DIFF####", 6666, 7777, false, false,true);
		
		RingoSocket ringo3 = new RingoSocket("TRANS###", 8888, 9999, false, false,true);
		
		RingoSocket ringo4 = new RingoSocket("TRANS###", 8899, 9988, false, false,true);
		
		ringo1.connectTo("localhost",7777 , false);
		
		ringo3.connectTo("localhost",7777 , false);
		
		ringo4.connectTo("localhost",7777 , false);
		
		Diff diff1 = new Diff(ringo1);
		
		Diff diff2 = new Diff(ringo2);
		
		Trans trans1 = new Trans(ringo3);
		
		Trans trans2 = new Trans(ringo4);
		
		diff1.input("bonjour".getBytes());
		
		diff2.input("salut a toi aussi".getBytes());
		
		System.out.println(new String(diff2.output()));
		
		System.out.println(new String(diff1.output()));
		
		trans1.input("coco.txt".getBytes());
		
	
	}
}
