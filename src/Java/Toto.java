import protocol.RingoSocket;
public class Toto {

	public static void main(String[] args) throws Exception {
		
		RingoSocket ringo1 = new RingoSocket("DIFF####", 4444, 7776, 9999,true);
		RingoSocket ringo2 = new RingoSocket("DIFF####", 4445, 7777, 9999,true);
		RingoSocket ringo3 = new RingoSocket("TRANS###", 4446, 7778, 9999,true);
		RingoSocket ringo4 = new RingoSocket("TRANS###", 4447, 7779, 9999,true);
		
		
		ringo1.connectTo("localhost",7777 , false);
		ringo3.connectTo("localhost",7777 , false);
		ringo4.connectTo("localhost",7777 , false);
		
		Diff diff1 = new Diff(ringo1);
		Diff diff2 = new Diff(ringo2);
		Trans trans1 = new Trans(ringo3);
		Trans trans2 = new Trans(ringo4);
		
		diff1.input("bonjour".getBytes());
		diff2.input("salut a toi aussi".getBytes());
		diff1.setVerbose(true);
		
		//ringo1.setVerbose(true);
		//trans1.setVerbose(true);
		/*
		for(int i=0; i<65001 ;i++){
			diff1.input("bonjour".getBytes());
		}
		*/
		trans1.input("coco.txt".getBytes());
		
		
		
		
		System.out.println(new String(diff2.output()));
		
		System.out.println(new String(diff1.output()));
		
		trans1.output();
	
		
	
		diff1.close();
		diff2.close();
		trans1.close();
		trans2.close();
		
	}
}
