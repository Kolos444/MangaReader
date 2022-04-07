import APIChapterClasses.APIChapter;
import APIChapterClasses.APIChapterListResponse;
import APIChapterClasses.APIChapterRelationships;
import APIClasses.APICoversResponse;
import APICustomListClasses.APISeasonalListResponse;
import APIMangaClasses.APIMangaListRelationships;
import APIMangaClasses.APIMangaListResponse;
import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class mainGUIMainPage {
	
	private static MangaReaderSingleton mangaReaderSingleton;
	
	public static Node buildMainGUIMainPage() throws IOException {
		mangaReaderSingleton = MangaReaderSingleton.instance();
		
		getMangaLists();
		
		return getStartPage();
	}
	
	public static Node getStartPage() throws IOException {
		VBox allItems = new VBox();
		allItems.getChildren().addAll(getSeasonalList(), getLatestUpdate(), getRecentlyAdded());
		return allItems;
	}
	
	private static HBox getSeasonalList() {
		HBox seasonalList = new HBox();
		//seasonalList.getChildren().addAll(buildSeasonalMangaListItem());
		return seasonalList;
	}
	
	
	private static Node getLatestUpdate() throws IOException {
		VBox      latestUpdate = new VBox();
		Chapter[] chapters     = getLatestChapters();
		
		Text latestUpdatedSection = new Text("Latest Updates");
		
		latestUpdate.getChildren().addAll(latestUpdatedSection);
		
		BorderPane[] borderPane = new BorderPane[6];
		VBox vBox[] = new VBox[6];
		
		for(int i = 0;i<4;i++){
			borderPane[i] = new BorderPane();
			vBox[i] = new VBox();
			for(int a = 0;a<6;a++)
				vBox[i].getChildren().add(buildChapterNode(chapters[i*6+a]));
			borderPane[i].setCenter(vBox[i]);
			latestUpdate.getChildren().add(borderPane[i]);
		}
		
		return latestUpdate;
	}
	
	private static Node buildChapterNode(Chapter chapter) {
		ImageView cover = new ImageView();
		cover.setImage(chapter.mangaCover);
		
		Text titleText = new Text(chapter.title);
		
		Text chapterInfo = new Text("Ch. " + chapter.chapterNumber + " - " + chapter.chapterTitle);
		
		Text group = new Text(chapter.group);
		
		Text updated = new Text(chapter.updatedAgo);
		
		BorderPane node            = new BorderPane();
		VBox       info           = new VBox();
		HBox       groupAndUpdated = new HBox();
		
		groupAndUpdated.getChildren().addAll(group,updated);
		info.getChildren().addAll(titleText,chapterInfo,groupAndUpdated);
		
		node.setLeft(cover);
		node.setCenter(info);
		
		return node;
	}
	
	private static Chapter[] getLatestChapters() throws IOException {
		List<Chapter> chapters     = new ArrayList<Chapter>() {};
		List<String>  neededCovers = new ArrayList<String>() {};
		
		for(APIChapter chapter : mangaReaderSingleton.mangaListLatestUpdate.data) {
			Chapter newChapter = new Chapter();
			
			newChapter.chapterTitle  = chapter.attributes.title;
			newChapter.chapterNumber = chapter.attributes.chapter;
			newChapter.updatedAgo    = chapter.attributes.readableAt;
			
			for(APIChapterRelationships relation : chapter.relationships) {
				if(relation.type == "scanlation_group")
					newChapter.group = relation.attributes.name;
				else if(relation.type == "manga") {
					newChapter.title = relation.attributes.title.en;
					neededCovers.add(relation.id);
				}
				if(newChapter.group != null && newChapter.title != null)
					break;
			}
			
			chapters.add(newChapter);
		}
		
		String[] coversArray = new String[neededCovers.size()];
		coversArray = neededCovers.toArray(coversArray);
		Image[] covers = getMultipleMangaCovers(coversArray);
		
		for(int i = 0; i < covers.length; i++)
			chapters.get(i).mangaCover = covers[i];
		
		Chapter[] chaptersArray = new Chapter[coversArray.length];
		return chapters.toArray(chaptersArray);
	}
	
	private static Image[] getMultipleMangaCovers(String[] ids) throws IOException {
		
		ids = getCoverArtFileNames(ids);
		
		Image[] covers      = new Image[ids.length];
		String  baseHttpUrl = "https://uploads.mangadex.org/covers/";
		
		int i = 0;
		for(String id : ids) {
			covers[i] = new Image(baseHttpUrl + id);
			i++;
		}
		
		return covers;
	}
	
	private static String[] getCoverArtFileNames(String[] ids) throws IOException {
		String baseHttpUrl = "https://api.mangadex.org/manga?includes[]=cover_art&order[followedCount]=desc";
		for(String id : ids) {
			baseHttpUrl += "&ids[]=" + id;
		}
		baseHttpUrl += "&limit=" + ids.length +
					   "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic";
		
		HttpURLConnection connection = HTTP.getHttpResponse(baseHttpUrl, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader    inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson              gson        = new Gson();
			APICoversResponse mangaArray  = gson.fromJson(inputReader, APICoversResponse.class);
			
			int i = 0;
			for(APIMangaListRelationships relation : mangaArray.data) {
				ids[i] += "/" + relation.attributes.fileName;
				i++;
			}
			return ids;
		}
		
		
		return new String[0];
	}
	
	private static HBox getRecentlyAdded() {
		HBox recentlyAdded = new HBox();
		return recentlyAdded;
	}
	
	private static void getMangaLists() throws IOException {
		
		mangaReaderSingleton.mangaListLatestUpdate  = getChapters(
				"https://api.mangadex.org/chapter?includes[]=manga&includes[]=scanlation_group&limit=24&translated" +
				"Language[]=en&translatedLanguage[]=de&contentRating[]=safe&contentRating[]=suggestive&" +
				"contentRating[]=erotica&order[readableAt]=desc");
		mangaReaderSingleton.mangaListRecentlyAdded = getMangas(
				"https://api.mangadex.org/manga?limit=20&contentRating[]=safe&contentRating[]=suggestive&" +
				"contentRating[]=erotica&order[createdAt]=desc&includes[]=cover_art");
		mangaReaderSingleton.mangaListSeasonalAdded =
				getMangasCustomList("https://api.mangadex.org/list/ff210dec-862b-4c17-8608-0e7f97c70488");
		
		
	}
	
	private static APIChapterListResponse getChapters(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader         inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson                   gson        = new Gson();
			APIChapterListResponse mangaArray  = gson.fromJson(inputReader, APIChapterListResponse.class);
			return mangaArray;
		}
		return new APIChapterListResponse();
	}
	
	private static APIMangaListResponse getMangas(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader       inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson                 gson        = new Gson();
			APIMangaListResponse mangaArray  = gson.fromJson(inputReader, APIMangaListResponse.class);
			return mangaArray;
		}
		return new APIMangaListResponse();
	}
	
	private static APISeasonalListResponse getMangasCustomList(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader          inputReader =
					new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson                    gson        = new Gson();
			APISeasonalListResponse mangaArray  = gson.fromJson(inputReader, APISeasonalListResponse.class);
			return mangaArray;
		}
		return new APISeasonalListResponse();
	}
	
	private static Node[] buildSeasonalMangaListItem() {
		
		ImageView imageView = new ImageView();
		Image     image     = new Image("");
		imageView.setImage(image);
		
		return new Node[0];
	}
}