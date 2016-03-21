
public class Serv implements ActionServ{
	
	
	private Id id;
	private udp port1;
	private udp port2;
	
	private tcp

	
	public Serv(udp a, Id id) {
		super();
		this.port1 = a;
		this.id = id;
	}

	public Dedoubler(upd b){
		if (this.port2 !=null){
			this.port2=b;
		}
		else{
			//TODO
		}
	}
	public udp getB() {
		return port2;
	}

	public void setB(udp b) {
		this.port2 = b;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

}
