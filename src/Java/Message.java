
public class Message {
	
	private int id;
	private String contenu;
	
	
	public Message(int id, String contenu) {
		super();
		this.setId(id);
		this.contenu = contenu;
	}

	public String getContenu() {
		return contenu;
	}


	public void setContenu(String contenu) {
		this.contenu = contenu;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	
	

}
