import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	class infoTransfert {
		public long actual_no_mess;
		public long num_mess;
		public BufferedOutputStream outputStream;
		public File file;

		public infoTransfert( long actual_no_mess,long num_mess) {
			super();
			
			this.actual_no_mess = actual_no_mess;
			this.num_mess = num_mess;
		}
	}
	
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
	private HashMap<Long, infoTransfert> id_TransMAP;
	
	private volatile String nameFileAsk;
	
	private final int maxSizeContent = Ringo.maxSizeMsg - (Ringo.byteSizeType + (Ringo.byteSizeSpace * 7)
			+ Ringo.byteSizeIdm * 3 + Ringo.byteSizeIdApp + byteSizeTransType +byteSizeContent) ;

	private final int byteSizeStart=byteSizeTransType+Ringo.byteSizeSpace*4+byteSizeId_Trans;
	private final int byteSizeDataROK_withoutName_FILE =byteSizeStart+byteSizeNom+byteSizeNum_Mess;
	private final int byteSizeDataSEN_withContent=byteSizeStart+byteSizeNo_Mess+byteSizeContent;
	public Trans(Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException {
		
		super("TRANS###", udpPort, tcpPort, verbose);
		
		this.id_TransMAP=new HashMap<Long, infoTransfert>(10);
		
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
						byte[] msgInByte = msgIN.getData_app();
						int curseur;
						String affichage=style + "\n"+LocalDateTime.now() +" -> " + "RECEVE : ";
						
						String type = new String(msgInByte, 0, byteSizeTransType);
						
						curseur=byteSizeTransType+Ringo.byteSizeSpace;
						
						if(type.equals("REQ")){
							req(affichage,msgInByte,curseur);
						}
						else if(type.equals("ROK")){
							rok(affichage,msgInByte,curseur);
						}
						
						else if(type.equals("SEN")){
							sen(affichage,msgInByte,curseur);
						}

					} catch (DOWNmessageException e) {
						System.out.println("THREAD: APP RECEVE | DOWNmessageException , the socket is CLOSE");
						runContinue = false;
					} catch (IOException e) {
						System.out.println("THREAD: APP RECEVE | File error");
					} catch (numberOfBytesException |InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
	
	private void sen(String affichage, byte[] msgInByte, int curseur)throws DOWNmessageException, IOException{
		affichage+= "SEN " ;
		byte [] id_transByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeId_Trans);
		Long id_trans=Message.byteArrayToLong(id_transByte, byteSizeId_Trans, ByteOrder.nativeOrder());
		curseur+=byteSizeId_Trans+Ringo.byteSizeSpace;
		infoTransfert value =id_TransMAP.get(id_trans);
		if(value==null){
			ringoSocket.send(msgIN);
			return;
		}
		
		byte [] no_messByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeNo_Mess);
		Long no_mess=Message.byteArrayToLong(no_messByte, byteSizeNo_Mess, ByteOrder.nativeOrder());
		curseur+=byteSizeNo_Mess+Ringo.byteSizeSpace;
		
		String size_contentSTR = new String(msgInByte,curseur,byteSizeContent);
		int size_content = Integer.parseInt(size_contentSTR);
		curseur+=byteSizeContent+Ringo.byteSizeSpace;
		
		
		if(!(value.actual_no_mess==no_mess)){
			System.out.println("problem d'ordre");
			System.out.println("valeur attentdu "+value.actual_no_mess);
			System.out.println("valeur recu "+no_mess);
			return;
		}
		File temp;
		if(value.actual_no_mess==0){
			System.out.println("premiere partie");
			temp = File.createTempFile("tempfile", ".tmp");
			value.file=temp;
			value.outputStream=new BufferedOutputStream(new FileOutputStream(temp));	
		}

		value.outputStream.write(msgInByte,curseur,size_content);
		value.outputStream.flush();
		value.actual_no_mess++;
		System.out.println("val nomess "+value.actual_no_mess);
		System.out.println("val nummess "+value.num_mess);
		System.out.println("ecris dedans "+new String(msgInByte,curseur,size_content));
		if(value.actual_no_mess==value.num_mess){
			System.out.println("transfert FINI");
			value.outputStream.close();
			updateFileList(value.file);
			
		}
		
	}
	
	private void rok(String affichage, byte[] msgInByte, int curseur) throws DOWNmessageException{
		affichage+= "ROK " ;
		byte [] id_transByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeId_Trans);
		
		Long id_trans=Message.byteArrayToLong(id_transByte, byteSizeId_Trans, ByteOrder.nativeOrder());
		curseur+=byteSizeId_Trans+Ringo.byteSizeSpace;
		
		String size_nom_STR = new String(msgInByte,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(msgInByte, curseur,tailleNameFile);
		
		if(name_fileSTR.equals(nameFileAsk)){
			byte [] num_messByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeNum_Mess);
			Long num_mess=Message.byteArrayToLong(num_messByte, byteSizeNum_Mess, ByteOrder.LITTLE_ENDIAN);
			
			id_TransMAP.put(id_trans,new infoTransfert(0L,num_mess) );	
		}
		else{
			ringoSocket.send(msgIN);// renvoi sur l'anneau du message
		}
	}
	
	private void req(String affichage, byte[] msgInByte, int curseur) throws IOException, DOWNmessageException, numberOfBytesException, InterruptedException{
		String size_nom_STR = new String(msgInByte,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(msgInByte, curseur,tailleNameFile);
		
		affichage+= "REQ " + name_fileSTR;
		System.out.println(affichage+ "\n" + style);
		
		Path pathFile=files.get(name_fileSTR);
		if(pathFile!=null){
			System.out.print("FILE IS HERE | ");
			
			byte []  debutMsg= "ROK".getBytes();
			long idt= ringoSocket.getUniqueIdm();
			System.out.println("id transaction "+idt);
			byte [] idTrans=Message.longToByteArray(idt, byteSizeId_Trans,ByteOrder.LITTLE_ENDIAN );
			byte [] size_nom=size_nom_STR.getBytes();
			byte [] name_file=name_fileSTR.getBytes();
			System.out.println("SIZE OF FILE :"+Files.size(pathFile));
			long num_messLong = Files.size(pathFile)/maxSizeContent;
			if(num_messLong<1){
				num_messLong=1;
			}
			byte [] num_mess =Message.longToByteArray(num_messLong, byteSizeNum_Mess, ByteOrder.LITTLE_ENDIAN);
			byte [] SPACE =" ".getBytes();
			byte [] data = new byte [byteSizeDataROK_withoutName_FILE+tailleNameFile];
			
			Message.remplirData(data,debutMsg,SPACE,idTrans,SPACE,size_nom,SPACE,name_file,SPACE,num_mess);
			ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", data));
			
			debutMsg="SEN".getBytes();
			
			File file=pathFile.toFile();
			BufferedInputStream out=new BufferedInputStream(new FileInputStream(file));
			byte [] content = new byte[maxSizeContent];
			int size_contentVal;
			byte [] no_mess;
			byte [] size_content;
			for(long i=0; i<num_messLong ; i++){
				size_contentVal=out.read(content);
				no_mess=Message.longToByteArray( i, byteSizeNo_Mess, ByteOrder.LITTLE_ENDIAN);
				size_content=Message.longToStringRepresentation(size_contentVal, 3).getBytes();
				data= new byte[500];
				Message.remplirData(data, debutMsg,SPACE,idTrans,SPACE,no_mess,SPACE,size_content,SPACE,content);
				ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", data));
				
			}
			out.close();
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