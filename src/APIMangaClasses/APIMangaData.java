package APIMangaClasses;

import javafx.scene.image.Image;

public class APIMangaData {
	public String id;
	public APIMangaListAttributes attributes;
	public APIMangaListRelationships[] relationships;

	//Nicht von der API
	public Image cover;
	public double rating;
	public int follows;

}
