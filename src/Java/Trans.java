import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.BindException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import Protocol.Message;
import Protocol.Ringo;
import Protocol.Exceptions.*;

public class Trans extends Appl {
	
	private final int byteSizeTransType=3;
	
	private HashMap<String, Path> files;
	
	public final static int byteSizeNom = 2;
	public final static int byteSizeNum_Mess = 8;
	public final static int byteSizeNo_Mess = 8;
	public final static int byteSizeId_Trans = 8;
	public final static int byteSizeContent = 3;
	
	/**
	 * contient les Id_Trans en cour dans cette application
	 * clef un Id-trens en COUR
	 * valeur [noMess actuel,nuMmess attendu]
	 */
	private HashMap<Long, Long[]> id_TransMAP;
	
	private volatile String nameFileAsk;
	
	private final int MaxDataByMessage = Ringo.maxSizeMsg - (Ringo.byteSizeType + (Ringo.byteSizeSpace * 7)
			+ Ringo.byteSizeIdm * 3 + Ringo.byteSizeIdApp + byteSizeTransType +byteSizeContent) ;

	private final int byteSizeStart=byteSizeTransType+Ringo.byteSizeSpace*4+byteSizeId_Trans;
	private final int byteSizeDataROK_withoutName_FILE =byteSizeStart+byteSizeNom+byteSizeNum_Mess;
	private final int byteSizeDataSEN_withContent=byteSizeStart+byteSizeNo_Mess+byteSizeContent;
	public Trans(Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException {
		
		super("TRANS###", udpPort, tcpPort, verbose);
		
		this.id_TransMAP=new HashMap<Long, Long[]>(10);
		
		/*
		 * Remplir la HashMap contenant les fichiers du repertoire courant
		 */
		File[] fileList= new File("").getAbsoluteFile().listFiles();
		files= new HashMap<String,Path>();
		if(fileList!=null){
			for (File f : fileList) {
			    if (f.isFile()) {
			    	System.out.println("nom du fichier present "+f.getName());
			    	files.put(f.getName(),f.toPath());
			    }
			}
		}
		
		Runnable runRecev = new Runnable() {
			public void run() {
				while (runContinue) {
					try {
						msgIN = ringoSocket.receive();
						byte[] content = msgIN.getData_app();
						int curseur;
						String affichage=style + "\n"+LocalDateTime.now() +" -> " + "RECEVE : ";
						
						String type = new String(content, 0, byteSizeTransType);
						
						curseur=byteSizeTransType+Ringo.byteSizeSpace;
						
						if(type.equals("REQ")){
							req(affichage,content,curseur);
						}
						else if(type.equals("ROK")){
							rok(affichage,content,curseur);
						}
						
						else if(type.equals("SEN")){
							sen(affichage,content,curseur);
						}

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
							String contenu = "REQ "+Message.longToStringRepresentation(input.length(),byteSizeNom)+ " " + input;
							ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", contenu.getBytes()));
							nameFileAsk=input;
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
	
	private void sen(String affichage, byte[] content, int curseur){
		affichage+= "SEN " ;
	}
	
	private void rok(String affichage, byte[] content, int curseur){
		affichage+= "ROK " ;
		byte [] id_trans=Arrays.copyOfRange(content,curseur, byteSizeId_Trans);
		Long id_transLong=Message.byteArrayToLong(id_trans, byteSizeId_Trans, ByteOrder.nativeOrder());
		curseur+=byteSizeTransType+Ringo.byteSizeSpace;
		
		String size_nom_STR = new String(content,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(content, curseur,tailleNameFile);
	
		
	}
	
	
	
	private void req(String affichage, byte[] content, int curseur) throws IOException, DOWNmessageException{
		String size_nom_STR = new String(content,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(content, curseur,tailleNameFile);
		
		affichage+= "REQ " + name_fileSTR;
		System.out.println(affichage+ "\n" + style);
		
		Path pathFile=files.get(name_fileSTR);
		if(pathFile!=null){
			System.out.print("FILE IS HERE | ");
			
			byte []  debutMsg= "ROK".getBytes();
			long idt= ringoSocket.getUniqueIdm();
			byte [] idTrans=Message.longToByteArray(idt, 8,ByteOrder.LITTLE_ENDIAN );
			byte [] size_nom=size_nom_STR.getBytes();
			byte [] name_file=name_fileSTR.getBytes();
			System.out.println("SIZE OF FILE :"+Files.size(pathFile));
			long num_messLong = Files.size(pathFile)/MaxDataByMessage;
			byte [] num_mess =Message.longToByteArray(num_messLong, 8, ByteOrder.LITTLE_ENDIAN);
			byte [] SPACE =" ".getBytes();
			
			byte [] data = new byte [byteSizeDataROK_withoutName_FILE+tailleNameFile];
			
			Message.remplirData(data,debutMsg,SPACE,idTrans,SPACE,size_nom,SPACE,name_file,SPACE,num_mess);
			ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", data));
			
			debutMsg="SEN".getBytes();
			for(long i=0; i<num_messLong ; i++){
				byte [] no_mess=Message.longToByteArray( i, 8, ByteOrder.LITTLE_ENDIAN);
				
				data= new byte[byteSizeDataSEN_withContent+999];
				
				
			}
		}else{
			ringoSocket.send(msgIN);// renvoi sur l'anneau du message
		}
	}
	
	/**
	 * Quand un fichier est nouvelement recu , il peut etre ajouter a la hashMap des fichiers du repertoire
	 * @param newFile le nouveau fichier recu par le reseau
	 */
	private void updateFileList(File newFile){
		this.files.put(newFile.getName(), newFile.toPath());
	}

	public static void main(String[] args) {

		boolean verbose=Appl.start(args);

		try {
			new Trans(Integer.parseInt(args[0]), Integer.parseInt(args[1]),verbose);

		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}