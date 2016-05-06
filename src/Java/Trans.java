import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.BindException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import protocol.*;
import protocol.exceptions.*;
import application.*;

public class Trans extends Appl implements ReceveSend {

	private class infoTransfert {
		public long actual_no_mess;
		public long num_mess;
		public BufferedOutputStream outputStream;
		public Path path;
		public String nameFile;

	public infoTransfert( long actual_no_mess,long num_mess,String nameFile) {
			super();
			this.actual_no_mess = actual_no_mess;
			this.num_mess = num_mess;
			this.nameFile=nameFile;
		}
	}
	
	private final int byteSizeTransType=3;
	
	private HashMap<String, Path> files;
	
	private Object ROKisComeBack=new Object();//mutex
	private boolean ROKisComeBackBool;
	private volatile String REQ_AskFile;
	
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
	
	private final int maxSizeContent = Ringo.maxSizeMsg - (Ringo.byteSizeType + (Ringo.byteSizeSpace * 7)
			+ Ringo.byteSizeIdm * 3 + Ringo.byteSizeIdApp + byteSizeTransType +byteSizeContent) ;
	private final int byteSizeStart=byteSizeTransType+Ringo.byteSizeSpace*4+byteSizeId_Trans;
	private final int byteSizeDataROK_withoutName_FILE =byteSizeStart+byteSizeNom+byteSizeNum_Mess;
	
	private final int byteSizeDataSEN_withContent=byteSizeStart+byteSizeNo_Mess+byteSizeContent;
	
	/**
	 * Application
	 */
	public Trans(String ip,Integer udpPort, Integer tcpPort,Integer multiPort,boolean verbose) throws BindException, IOException, ParseException {
		super(ip,"TRANS###", udpPort, tcpPort ,multiPort,verbose);
		this.initTrans();
	}
	
	/**
	 * Service
	 */
	public Trans(RingoSocket ringosocket){
		super("TRANS###",ringosocket);
		this.initTrans();
	}
	
	private void initTrans(){
		this.id_TransMAP=new HashMap<Long, infoTransfert>(10);
		initFileHash();
		initThread(new MyRunnableReceve(this),new MyRunnableSend(this));
	}
	
	/*
	 * Remplir la HashMap contenant les fichiers du repertoire courant
	 */
	private void initFileHash(){
		File[] fileList= new File("").getAbsoluteFile().listFiles();
		files= new HashMap<String,Path>();
		if(fileList!=null){
			for (File f : fileList) {
			    if (f.isFile()) {
			    	printModeApplication("nom du fichier present "+f.getName());
			    	files.put(f.getName(),f.toPath());
			    }
			}
		}
	}
	
	public void doReceve(Message msg) throws RingoSocketCloseException, IOException, NumberOfBytesException, InterruptedException, ParseException {
		byte[] msgInByte =msg.getData_app();
		int curseur;
		String affichage=style + "\n"+LocalDateTime.now() +" -> " + "RECEVE : ";
		
		String type = new String(msgInByte, 0, byteSizeTransType);
		
		curseur=byteSizeTransType+Ringo.byteSizeSpace;
		boolean msgForThisAPPL=false;
		
		if(type.equals("REQ")){
			msgForThisAPPL=req(affichage,msgInByte,curseur);
		}
		else if(type.equals("ROK")){
			msgForThisAPPL=rok(affichage,msgInByte,curseur);
		}
		else if(type.equals("SEN")){
			msgForThisAPPL=sen(affichage,msgInByte,curseur);
		}
		if(!msgForThisAPPL){
			super.ringoSocket.send(msg);
		}
	}
	
	
	/**
	 * Procedure en cas de message SEN recu
	 * @param affichage
	 * @param msgInByte
	 * @param curseur
	 * @return true si le message etait destiner a cette entity
	 * @throws RingoSocketCloseException
	 * @throws IOException
	 */
	private boolean sen(String affichage, byte[] msgInByte, int curseur)throws RingoSocketCloseException, IOException{
		affichage+= "SEN " ;
		byte [] id_transByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeId_Trans);
		Long id_trans=Message.byteArrayToLong(id_transByte, byteSizeId_Trans, ByteOrder.nativeOrder());
		curseur+=byteSizeId_Trans+Ringo.byteSizeSpace;
		infoTransfert value =id_TransMAP.get(id_trans);
		if(value==null){
			return false;
		}
		
		byte [] no_messByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeNo_Mess);
		Long no_mess=Message.byteArrayToLong(no_messByte, byteSizeNo_Mess, ByteOrder.LITTLE_ENDIAN);
		
		curseur+=byteSizeNo_Mess+Ringo.byteSizeSpace;
		
		String size_contentSTR = new String(msgInByte,curseur,byteSizeContent);
		int size_content = Integer.parseInt(size_contentSTR);
		curseur+=byteSizeContent+Ringo.byteSizeSpace;
		
		
		if(!(value.actual_no_mess==no_mess)){
			printModeApplication("problem d'ordre");
			printModeApplication("valeur attentdu "+value.actual_no_mess);
			printModeApplication("valeur recu "+no_mess);
			return true;//TODO quand ordre pas respecter
		}
		if(value.actual_no_mess==0){
			printModeApplication("premiere partie");
			Path pathNewFile;
			
			if(new File("./"+value.nameFile).isFile()){
				int i=1;
				while(new File("./"+value.nameFile+i).isFile()){
					i++;
				}
				pathNewFile = Paths.get("./"+value.nameFile+i);
			}else{
				pathNewFile = Paths.get("./"+value.nameFile);
			}

			BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(pathNewFile, CREATE, APPEND));
			value.outputStream=out;
			value.path =pathNewFile;
		}

		value.outputStream.write(msgInByte,curseur,size_content);
		
		value.actual_no_mess++;
		double p1=(double)(value.actual_no_mess);
		double p2=(double)(value.num_mess);
		int pourcentage =(int) ((p1/p2)*100.0);
		printModeApplication(style+"\n"+pourcentage+"% TRANSMIS");
		//printModeApplication("ecris dedans "+new String(msgInByte,curseur,size_content));
		if(value.actual_no_mess==value.num_mess){
			value.outputStream.flush();
			value.outputStream.close();
			updateFileList(value.nameFile,value.path);
			printModeApplication(style+"\ntransfert FINI | new File : "+value.path.getFileName()+"\n"+style);
		}
		return true;
	}
	
	/**
	 * Procedure en cas de message ROK recu
	 * @param affichage
	 * @param msgInByte
	 * @param curseur
	 * @return true si le message etait destiner a cette entity
	 * @throws RingoSocketCloseException
	 */
	private boolean rok(String affichage, byte[] msgInByte, int curseur) throws RingoSocketCloseException{
		affichage+= "ROK " ;
		byte [] id_transByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeId_Trans);
		
		Long id_trans=Message.byteArrayToLong(id_transByte, byteSizeId_Trans, ByteOrder.nativeOrder());
		curseur+=byteSizeId_Trans+Ringo.byteSizeSpace;
		
		String size_nom_STR = new String(msgInByte,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(msgInByte, curseur,tailleNameFile);
		curseur+=tailleNameFile+Ringo.byteSizeSpace;
		
		if(name_fileSTR.equals(REQ_AskFile)){
			synchronized (ROKisComeBack) {
				ROKisComeBackBool=true;
				ROKisComeBack.notify();
			}
			byte [] num_messByte=Arrays.copyOfRange(msgInByte,curseur, curseur+byteSizeNum_Mess);
			Long num_mess=Message.byteArrayToLong(num_messByte, byteSizeNum_Mess, ByteOrder.LITTLE_ENDIAN);
			
			id_TransMAP.put(id_trans,new infoTransfert(0L,num_mess,name_fileSTR) );	
			printModeApplication("THE TRANSFERT CAN START");
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Procedure en cas de message REQ recu
	 * @param affichage
	 * @param msgInByte
	 * @param curseur
	 * @return true si le message etait destiner a cette entity
	 * @throws IOException
	 * @throws RingoSocketCloseException
	 * @throws numberOfBytesException
	 * @throws InterruptedException
	 * @throws ParseException 
	 */
	private boolean req(String affichage, byte[] msgInByte, int curseur) throws IOException, RingoSocketCloseException, NumberOfBytesException, InterruptedException, ParseException{
		String size_nom_STR = new String(msgInByte,curseur,byteSizeNom);
		int tailleNameFile = Integer.parseInt(size_nom_STR);
		curseur+=byteSizeNom+Ringo.byteSizeSpace;
		String name_fileSTR = new String(msgInByte, curseur,tailleNameFile);
		
		affichage+= "REQ " + name_fileSTR;
		printModeApplication(affichage+ "\n" + style);
		
		Path pathFile=files.get(name_fileSTR);
		if(pathFile!=null){
			printModeApplication("FILE IS HERE | ");
			
			byte []  debutMsg= "ROK".getBytes();
			long idt= ringoSocket.getUniqueIdm();
			printModeApplication("id transaction "+idt);
			byte [] idTrans=Message.longToByteArray(idt, byteSizeId_Trans,ByteOrder.LITTLE_ENDIAN );
			byte [] size_nom=size_nom_STR.getBytes();
			byte [] name_file=name_fileSTR.getBytes();
			printModeApplication("SIZE OF FILE :"+Files.size(pathFile));
			long num_messLong = Files.size(pathFile)/maxSizeContent;
			if(num_messLong<1){
				num_messLong=1L;
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
				data= new byte[500];//TODO
				Message.remplirData(data, debutMsg,SPACE,idTrans,SPACE,no_mess,SPACE,size_content,SPACE,content);
				ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", data));
				
			}
			out.close();
			return true;
		}else{
			return false;
		}
	}
	
	private void updateFileList(String name , Path path){
		this.files.put(name,path);
	}

	public void doSend() throws NumberOfBytesException, RingoSocketCloseException, InterruptedException, ParseException {
		String contenu = "REQ " + Message.longToStringRepresentation(input.length(), byteSizeNom)+ " " + input;
		
		ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "TRANS###", contenu.getBytes()));
		
		synchronized (ROKisComeBack) {
			REQ_AskFile = input;
			
			printModeApplication("fichier attendu "+ REQ_AskFile);
			ROKisComeBackBool = false;
			int timeMax=5000;
			
			long startTime = System.currentTimeMillis();
			ROKisComeBack.wait(timeMax);
			long endTime   = System.currentTimeMillis();
			if (!ROKisComeBackBool) {
				printModeApplication("ROK is not comeback in time : "+timeMax);
			}else{
				long totalTime = endTime - startTime;
				printModeApplication(style+"\nTIME WAITED :"+totalTime+"\n");
			}
			REQ_AskFile = "";
		}
	}
	
	public static void main(String[] args) {

		boolean verbose=Appl.testArgs(args);

		try {
			String ip = Appl.selectIp();
			new Trans(ip,Integer.parseInt(args[0]), Integer.parseInt(args[1]),Integer.parseInt(args[2]),verbose);

		} catch (BindException | ParseException e) {
			System.out.println("The ports are already in use or are bigger than 4digit");
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}