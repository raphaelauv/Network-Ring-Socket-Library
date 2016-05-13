import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import application.Diff;
import application.Trans;
import protocol.Message;
import protocol.RingoSocket;
public class Toto {

	public static void main(String[] args) throws Exception {
		
		RingoSocket ringo1 = new RingoSocket("DIFF####", 4444, 7776, 9999,true);
		RingoSocket ringo2 = new RingoSocket("DIFF####", 4445, 7777, 9999,true);
		RingoSocket ringo3 = new RingoSocket("TRANS###", 4446, 7778, 9999,true);
		RingoSocket ringo4 = new RingoSocket("TRANS###", 4447, 7779, 9999,true);
		RingoSocket ringo5 = new RingoSocket("DIFF####", 4448, 7780, 9999,true);
		

		ringo4.connectTo("localhost",7777 , false);
		ringo5.connectTo("localhost",7777 , false);
		ringo1.connectTo("localhost",7777 , false);
		ringo3.connectTo("localhost",7777 , false);
		
		
		Diff diff1 = new Diff(ringo1);
		Diff diff2 = new Diff(ringo2);
		Trans trans1 = new Trans(ringo3);
		Trans trans2 = new Trans(ringo4);
		Diff diff3 = new Diff(ringo5);
				

		
		diff1.doSend("bijour");
		diff2.doSend("salut a toi");
		//diff1.setVerbose(true);
		//trans1.setVerbose(true);
		//ringo1.setVerbose(true);
		//trans1.setVerbose(true);
		/*
		for(int i=0; i<65001 ;i++){
			diff1.doSend("bonjour");
		}
		*/
		//trans1.doSend("coco.txt");
		
		

		System.out.println("diff 3 recoit :"+new String(diff3.output()));
		
		System.out.println("diff 3 recoit :"+new String(diff3.output()));		
		
		System.out.println("diff 2 recoit :"+new String(diff2.output()));
		
		System.out.println("diff 1 recoit :"+new String(diff1.output()));

		
		//System.out.println(new String(trans1.output()));

	
		//trans1.setVerbose(true);
		//ringo1.setVerbose(true);
		//trans1.setVerbose(true);
		//trans2.setVerbose(true);
		
		
		
		diff1.close();
		diff2.close();
		trans1.close();
		trans2.close();
		diff3.close();
		
		
		
		
		String idApp="LAMBDA##";
		RingoSocket ringoManuel = new RingoSocket(idApp, 4450, 7788, 9999,true);
		RingoSocket ringoManuel2 = new RingoSocket(idApp, 4455, 7789, 9999,true);
		RingoSocket ringoManuel3 = new RingoSocket(idApp, 4465, 7799, 9999,true);

		
		ringoManuel.connectTo("localhost", 7789, false);
		ringoManuel3.connectTo("localhost", 7789, true);
		
		ringoManuel.test(false);
		
		HashMap<InetSocketAddress, String> members=ringoManuel.whos();
		
		for (Map.Entry<InetSocketAddress, String> entry : members.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}
		
		ringoManuel.isClose();
		
		ringoManuel.send(Message.APPL(ringoManuel.getUniqueIdm(),idApp , "juste pour le fun".getBytes()));
		
		Message recu=ringoManuel2.receive();
		
		System.out.println(new String(recu.getData_app()));
		
		ringoManuel2.disconnect();//TODO
	
		ringoManuel.down();
		
	
		System.out.println(ringoManuel.isClose());//TODO
	}
}
