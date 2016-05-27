package application.core;

import protocol.*;
import protocol.exceptions.*;
import java.io.IOException;
import java.net.BindException;


public abstract class ApplSendReceve extends Appl {


	/**
	 * Constructeur pour application independante , ecoute sur STDIN et ecris sur STDOUT
	 */
	public ApplSendReceve(String ip,String APPLID,Integer udpPort, Integer tcpPort,Integer multiPort,boolean verbose) throws BindException,IOException, ParseException{
		super(ip,APPLID,udpPort,tcpPort,multiPort,verbose);
	}
	
	/**
	 * Constructeur pour service
	*/
	public ApplSendReceve(String APPLID,RingoSocket ringoSocket){
		super(APPLID,ringoSocket);
	}
	
	public byte[] receve() throws Exception, InterruptedException{
		if(!modeService){
			throw new Exception();
		}
		synchronized (listOutput) {
			while (listOutput.isEmpty()) {
				listOutput.wait();
			}
			return listOutput.pop();
		}
	}
	
	protected abstract void doReceve(Message msg) throws RingoSocketCloseException, IOException, NumberOfBytesException, InterruptedException, ParseException;
	
	protected abstract void send(String input) throws NumberOfBytesException, RingoSocketCloseException, InterruptedException, ParseException, IOException;
	
	
	/**
	 * Pour initialiser les threads , les nommer puis les lancer
	 * @param receve
	 * @param send
	 * @param name nom de l'APPL
	 */
	protected void initThread(MyRunnableReceve runnableReceve,MyRunnableSend runnableSend){
	
		this.ThRecev=new Thread(runnableReceve);
		this.ThSend=new Thread(runnableSend);
		this.ThRecev.setName(APPLID+" RECE");
		this.ThSend.setName(APPLID+" SEND ");
		if(modeService){
			this.ThRecev.setDaemon(true);
			this.ThSend.setDaemon(true);
		}
		this.ThRecev.start();
		this.ThSend.start();
		
		
	}
	
}