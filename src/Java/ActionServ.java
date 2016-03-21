public interface Communication {
//cette interface defini les actions realisable par une applicationa avec son implementation reseaux

	public int envoyer(string message , int id);
		// demande l'envoy d'un message a l'entité id
	public string lire();
		//lit le premier message disponible dans la liste géré par l'implementation reseaux
	
}
