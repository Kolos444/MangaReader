import APIChapterClasses.APIChapterListResponse;
import APICustomListClasses.APISeasonalListResponse;
import APIMangaClasses.APIMangaListResponse;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class MangaReaderSingleton {

	private static final MangaReaderSingleton holyInstance = new MangaReaderSingleton();
	public               BorderPane           rootNode;
	public               Node                 homePage;

	private MangaReaderSingleton() {}

	public static MangaReaderSingleton instance() {
		return holyInstance;
	}


	//Wie Recently Added
	public APIMangaListResponse mangaList;

	//https://api.mangadex.org/chapter?includes[]=manga&includes[]=scanlation_group&limit=24&translatedLanguage[]=en&translatedLanguage[]=de&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&order[readableAt]=desc
	public APIChapterListResponse mangaListLatestUpdate;

	//https://api.mangadex.org/manga?limit=20&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&order[createdAt]=desc&includes[]=cover_art
	public APIMangaListResponse mangaListRecentlyAdded;

	//Diese URL ist für die Sommersaison 2022 und wird von Admins auf MangaDex manuell als personalisierte Liste
	// erstellt, weswegen diese URL ebenfalls manuell geändert werden muss
	//https://api.mangadex.org/list/1f43956d-9fe6-478e-9805-aa75ec0ac45e
	public APISeasonalListResponse mangaListSeasonalAdded;
	public double                  height, width;
	public BorderPane centerViewNode;

}
