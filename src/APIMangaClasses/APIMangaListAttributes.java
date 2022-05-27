package APIMangaClasses;

import APIClasses.APITitle;
import javafx.scene.image.Image;

public class APIMangaListAttributes {
	public APITitle       title;
	public APIAltTitles[] altTitles;
	public APIDescription description;
	public String         originalLanguage;
	public String[]       availableTranslatedLanguages;
	public String         status;
	public String         year;
	public String         contentRating;
	public APITags[]      tags;

	//Nicht von der API
	public Image  cover;
	public int    follows;
	public double rating;
}
