import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import Protocol.Message;
import Protocol.Ringo;
import Protocol.Exceptions.*;

public class Trans extends Appl {
	
	private final int sizeTransStype=3;
	private volatile int val = 0;
	private HashMap<String, Path> files;
	
	private final int MaxDataByMessage = Ringo.maxSizeMsg - (Ringo.byteSizeType + (Ringo.byteSizeSpace * 7)
			+ Ringo.byteSizeIdm * 3 + Ringo.byteSizeIdApp + sizeTransStype +Ringo.byteSizeContent) ;

	public Trans(Integer udpPort, Integer tcpPort) throws BindException, IOException {
		
		super("TRANS###", udpPort, tcpPort, true);
		
		File[] fileList= new File("").getAbsoluteFile().listFiles();
		
		
		files= new HashMap<String,Path>();
		if(fileList!=null){
			for (File f : fileList) {
			    if (f.isFile()) {
			    	files.put(f.getName(),f.toPath());
			    }
			}
		}
		
		int idTrans=10000000;
		Runnable runRecev = new Runnable() {
			public void run() {
				while (runContinue) {
					try {
						output = ringoSocket.receive();
						byte[] content = output.getData_app();
						int curseur;
						String affichage=style + "\n"+LocalDateTime.now() +" -> " + "RECEVE : ";
						
						
						String type = new String(content, 0, sizeTransStype);
						curseur=sizeTransStype+Ringo.byteSizeSpace;
						if(type.equals("REQ")){
							String tailleString = new String(content,curseur,curseur+Ringo.byteSizeNom);
							int taille = Integer.parseInt(tailleString);
							curseur+=Ringo.byteSizeNom+1;
							String nameFile = new String(content, curseur, curseur+taille);
							affichage+= "REQ " + nameFile + "\n" + style;
							
							Path pathFile=files.get(nameFile);
							if(pathFile!=null){
								long num_mess = Files.size(pathFile)/MaxDataByMessage;
								String contenu = "ROK "+idTrans+tailleString+" "+nameFile+" "+Message.longToByteArray(num_mess, 8,ByteOrder.LITTLE_ENDIAN );
								ringoSocket.send(Message.APPL(val, "TRANS###", contenu.getBytes()));
							}
						}
						
						else if(type.equals("SEN")){
							//affichage+= "SEN " + nameFile 
						}
						System.out.println(affichage+ "\n" + style);


					} catch (DOWNmessageException e) {
						System.out.println("THREAD: APP RECEVE | DOWNmessageException , the socket is CLOSE");
						runContinue = false;
					} catch (IOException e) {
						System.out.println("THREAD: APP RECEVE | File error");
						//e.printStackTrace();
					}
				}
				ThSend.interrupt();
			}
		};

		Runnable runSend = new Runnable() {
			public void run() {
				
				boolean entrytested;
				while (runContinue) {
					entrytested = testEntry();

					if (!entrytested) {
						try {
							val++;
							String contenu = "REQ "+Message.longToStringRepresentation(input.length(), Ringo.byteSizeNom) + " " + input;
							ringoSocket.send(Message.APPL(val, "TRANS###", contenu.getBytes()));
						} catch (numberOfBytesException e) {
							//TODO
							System.out.println("\nERREUR SizeMessageException !! the limit is : " + Ringo.maxSizeMsg);
						} catch (DOWNmessageException e) {
							System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
							runContinue = false;
						}

					}
					else{
						if(!runContinue){
							System.out.println("\nTHREAD: APP SEND   | END");
						}
					}
				}
				ThRecev.interrupt();
			}
		};

		initThread(runRecev,runSend,"TRANS");
	}
	
	private void updateFileList(File newFile){
		this.files.put(newFile.getName(), newFile.toPath());
	}

	public static void main(String[] args) {

		Appl.start(args);

		try {
			new Trans(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}