import APIChapterClasses.APIChapterListResponse;
import APIClasses.APILoginUser;
import APIClasses.APIToken;
import APICustomListClasses.APISeasonalListResponse;
import APIMangaClasses.APIMangaListResponse;
import javafx.scene.layout.BorderPane;

public class MangaReaderSingleton {

	private static final MangaReaderSingleton holyInstance = new MangaReaderSingleton();
	public               BorderPane           mainWindow;

	private MangaReaderSingleton() {}

	public static MangaReaderSingleton instance() {
		return holyInstance;
	}

	public APIToken     apiToken;
	public APILoginUser apiLoginUser;

	//Wie Recently Added
	public APIMangaListResponse mangaList;

	//https://api.mangadex.org/chapter?includes[]=manga&includes[]=scanlation_group&limit=24&translatedLanguage[]=en&translatedLanguage[]=de&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&order[readableAt]=desc
	public APIChapterListResponse mangaListLatestUpdate;

	//https://api.mangadex.org/manga?limit=20&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&order[createdAt]=desc&includes[]=cover_art
	public APIMangaListResponse mangaListRecentlyAdded;

	//https://api.mangadex.org/list/ff210dec-862b-4c17-8608-0e7f97c70488
	public APISeasonalListResponse mangaListSeasonalAdded;
	public double                  height, width;
	public BorderPane mainWindowBorder;

}
