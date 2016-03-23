import java.io.IOException;
import java.net.UnknownHostException;

public interface Communication {
//cette interface defini les actions realisable par une applicationa avec son implementation reseaux

	public void connectTo(String adresse ,int udp) throws AlreadyAllUdpPortSet, UnknownHostException, IOException;
	
	public void envoyer(String message , int id) throws SizeException;
		// demande l'envoy d'un message a l'entité id
	public String lire();
		//lit le premier message disponible dans la liste géré par l'implementation reseaux
	
}
