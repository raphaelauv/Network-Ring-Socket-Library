import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import application.Diff;
import application.Securise;
import application.Trans;
import protocol.Message;
import protocol.RingoSocket;
public class Toto {

	public static void main(String[] args) throws Exception {
		
		RingoSocket ringo1 = new RingoSocket("localhost","DIFF####", 4444, 7776, 9999,true);
		RingoSocket ringo2 = new RingoSocket("localhost","DIFF####", 4445, 7777, 9999,true);
		RingoSocket ringo3 = new RingoSocket("localhost","TRANS###", 4446, 7778, 9999,true);
		RingoSocket ringo4 = new RingoSocket("localhost","TRANS###", 4447, 7779, 9999,true);
		RingoSocket ringo5 = new RingoSocket("localhost","DIFF####", 4448, 7780, 9999,true);
		
		
		RingoSocket ringo6 = new RingoSocket("localhost",null, 4459, 7785, 9999,true);
		
		
		
		RingoSocket ringo7 = new RingoSocket("localhost","SECURISE", 4348, 7180, 9999,true);
		RingoSocket ringo8 = new RingoSocket("localhost","SECURISE", 4248, 7280, 9999,true);
		
		Diff diff1 = new Diff(ringo1);
		Diff diff2 = new Diff(ringo2);
		Trans trans1 = new Trans(ringo3);
		Trans trans2 = new Trans(ringo4);
		Diff diff3 = new Diff(ringo5);
		Entity enti = new Entity(ringo6);
		
		Securise secu1 = new Securise(ringo7, "papa");
		Securise secu2 = new Securise(ringo8, "mama");
		
		ringo4.connect("localhost",7777 , false);
		ringo5.connect(ringo2,false);
		ringo6.connect(ringo5,false);
		ringo1.connect(ringo2,false);
		ringo3.connect(ringo5,false);
		ringo7.connect(ringo2, false);
		ringo8.connect(ringo5, false);
		
		
		
		diff1.send("bijour");
		diff2.send("salut a toi");
		//diff1.setVerbose(true);
		//trans1.setVerbose(true);
		//ringo1.setVerbose(true);
		//trans1.setVerbose(true);
		
		trans1.send("papa");
		
		
		
		System.out.println("diff 3 recoit :"+new String(diff3.receve()));
		
		System.out.println("diff 3 recoit :"+new String(diff3.receve()));		
		
		System.out.println("diff 2 recoit :"+new String(diff2.receve()));
		
		System.out.println("diff 1 recoit :"+new String(diff1.receve()));

		System.out.println(new String(trans1.receve()));	
		//trans1.setVerbose(true);
		//ringo1.setVerbose(true);
		//trans1.setVerbose(true);
		//trans2.setVerbose(true);
		
		secu1.sendPublicKey();
		secu2.sendPublicKey();
		
		secu1.waitForPublicKey("mama");
		secu2.waitForPublicKey("papa");
		
		secu1.doSend("salut a madame", "mama");
		secu2.doSend("salut a monsieur", "papa");
		
		System.out.println("SECU 1 : papa  recoit :"+new String(secu1.receve()));
		System.out.println("SECU 2 : mama  recoit :"+new String(secu2.receve()));

		
		diff1.close();
		diff2.close();
		trans1.close();
		trans2.close();
		diff3.close();
		
		secu1.close();
		secu2.close();
		
		String idApp="LAMBDA##";
		RingoSocket ringoManuel = new RingoSocket("localhost",idApp, 4450, 7788, 9999,true);
		RingoSocket ringoManuel2 = new RingoSocket("localhost",idApp, 4455, 7789, 9999,true);
		
		String idApp3="cococo##";
		RingoSocket ringoManuel3 = new RingoSocket("localhost",idApp3, 4465, 7799, 9999,true);

		
		ringoManuel.connect("localhost", 7789, false);
		ringoManuel3.connect("localhost", 7789, true);
		
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
	
		//ringoManuel.down();
		
		ringoManuel.close();
	
		System.out.println(ringoManuel.isClose());//TODO
	}
}
