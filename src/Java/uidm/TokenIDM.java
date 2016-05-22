package uidm;

import java.io.IOException;
import java.util.LinkedList;
import protocol.Message;
import protocol.Ringo;

	class MessageToken extends Message{
	
	}
	
	enum TokenTypeMessage{
		TOKE;
	}

@Deprecated
public class TokenIDM extends UniqueIDM{
	
	Ringo ringoSocket;
	LinkedList<Long> listIDM;
	
	public TokenIDM(Ringo ringoSocket) {
		this.listIDM=new LinkedList<Long>();
		this.ringoSocket=ringoSocket;
	}
	
	@Override
	protected long LocalGetIDM() throws IOException {
		
		if(!this.listIDM.isEmpty()){
			return this.listIDM.removeFirst();
		}
		
		return 0L;
	}

	
}
