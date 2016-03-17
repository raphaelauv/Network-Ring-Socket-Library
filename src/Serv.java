
public class Serv implements ActionServ{
	
	private Port port1;
	private Port port2;
	
	private int id;

	
	public Serv(Port a, Port b, int id) {
		super();
		this.port1 = a;
		this.port2 = b;
		this.id = id;
	}

	public Port getB() {
		return port2;
	}

	public void setB(Port b) {
		this.port2 = b;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void AddConnect() {
		// TODO Auto-generated method stub
		
	}
}
