import APIChapterClasses.APIChapter;
import APIChapterClasses.APIChapterListResponse;
import APIChapterClasses.APIChapterRelationships;
import APIChapters.APICChaptersRelationships;
import APIChapters.APIChaptersData;
import APIChapters.APIChaptersResponse;
import APICustomListClasses.APISeasonalListResponse;
import APIMangaClasses.APIManga;
import APIMangaClasses.APIMangaListRelationships;
import APIMangaClasses.APIMangaListResponse;
import CoverAbfrage.CoverAbfrage;
import CoverAbfrage.CoverAbfrageData;
import CoverAbfrage.CoverAbfrageDataRelationships;
import com.google.gson.Gson;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class mainGUIMainPage {

	//Singleton mit wichtigen Klassenübergreifenden Daten
	private static MangaReaderSingleton mangaReaderSingleton;
	//Der derzeitig ausgewählte Manga
	private static APIManga             mangaObject;

	public static Node buildMainGUIMainPage() throws IOException {

		//Initialisierung des Singletons
		mangaReaderSingleton = MangaReaderSingleton.instance();

		//Updated die im Singleton Manga Listen mit den neusten, zuletzt aktualisierten und saisonal Mangas
		getMangaLists();

		//Gibt die Hauptanzeige der Anwendung zurück
		return getStartPage();
	}

	//Erstellt ung gibt die Hauptanzeige zurück
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

	//Eine Liste der zuletzt hinzugefügten Kapitel
	private static Node getLatestUpdate() throws IOException {

		//Container
		VBox core  = new VBox();
		HBox table = new HBox();

		//Besorgt die gewünschten Kapitel
		Chapter[] chapters = getLatestChapters();

		//Überschrift
		Text latestUpdatedSection = new Text("Latest Updates");
		table.getChildren().add(latestUpdatedSection);

		BorderPane[] borderPane = new BorderPane[6];
		VBox[]       vBox       = new VBox[6];

		for(int i = 0; i < 4; i++) {
			borderPane[i] = new BorderPane();
			vBox[i]       = new VBox();
			for(int a = 0; a < 6; a++)
				if(i * 6 + a < chapters.length)
					vBox[i].getChildren().add(buildChapterNode(chapters[i * 6 + a]));
			borderPane[i].setCenter(vBox[i]);
			table.getChildren().add(borderPane[i]);
		}
		core.getChildren().addAll(latestUpdatedSection, table);
		return core;
	}

	private static Node buildChapterNode(Chapter chapterData) {
		VBox       vBox       = new VBox();
		BorderPane borderPane = new BorderPane();

		Rectangle rectangle = new Rectangle(100.d, 100.d, mangaReaderSingleton.screenWidth * 0.0365d,
											mangaReaderSingleton.screenHeight * 0.092d);
		rectangle.setArcHeight(10.0d);
		rectangle.setArcWidth(10.0d);
		rectangle.setFill(new ImagePattern(chapterData.mangaCover));
		Group rectangleGroup = new Group(rectangle);
		borderPane.setLeft(rectangleGroup);

		VBox  otherVBox = new VBox();
		Label title;
		if(chapterData.title.length() > 25)
			title = new Label(chapterData.title.substring(0, 22) + "...");
		else
			title = new Label(chapterData.title);
		//title.set
		title.setId("latestUpdateTitle");

		Label chapter = new Label(chapterData.chapterNumber + chapterData.chapterTitle);
		chapter.setId("latestUpdateChapterTitle");

		Label group = new Label(chapterData.group);
		group.setId("latestUpdateGroup");

		String[] time  = chapterData.updatedAgo.split("-", 3);
		String[] time2 = time[2].substring(3).split(":", 3);
		LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(time[0]), Month.of(Integer.parseInt(time[1])),
													   Integer.parseInt(time[2].substring(0, 2)),
													   Integer.parseInt(time2[0]), Integer.parseInt(time2[1]),
													   Integer.parseInt(time[2].substring(0, 2)));
		Duration duration   = Duration.between(localDateTime, LocalDateTime.now(ZoneId.of("UTC")));
		Label    updatedAgo = new Label(duration.toHours() + " Hours " + duration.toMinutes() + " Minutes ago");
		updatedAgo.setId("latestUpdateUpdatedAgo");

		BorderPane node = new BorderPane();
		node.setLeft(group);
		node.setRight(updatedAgo);
		otherVBox.getChildren().addAll(title, chapter, node);

		borderPane.setCenter(otherVBox);

		borderPane.setMinWidth(mangaReaderSingleton.width / 4);
		borderPane.setMaxWidth(mangaReaderSingleton.width / 4);
		vBox.getChildren().add(borderPane);
		vBox.setOnMouseClicked(event -> {
			try {
				showManga(chapterData.id);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		return vBox;
	}

	private static void showManga(String id) throws IOException {
		mangaObject = getMangaObject(id);
		VBox back = new VBox();
		back.getChildren().addAll(buildMangaTop(), buildMangaChapters());
		mangaReaderSingleton.mainWindow.setCenter(back);

	}

	private static Node buildMangaTop() {
		VBox back = new VBox();
		back.getChildren().addAll(buildMangaPresentation(), buildMangaDescription());
		return back;
	}

	private static Node buildMangaPresentation() {
		HBox      back  = new HBox();
		ImageView cover = null;

		for(APIMangaListRelationships relationship : mangaObject.data.relationships) {
			if(relationship.type.equals("cover_art"))
				cover = new ImageView("https://uploads.mangadex.org/covers/" + relationship.id);
		}

		Label title = new Label(mangaObject.data.attributes.title.en);
		title.setId("mangaTitle");

		back.getChildren().addAll(cover, title);
		return back;
	}

	private static Node buildMangaDescription() {
		VBox back = new VBox();

		//javafx.scene.control.TextArea description =
		//TODO Kommenntar entfernen		new javafx.scene.control.TextArea(mangaObject.data.attributes.description.en);

		//back.getChildren().add(description);

		return back;
	}

	private static Node buildMangaChapters() throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(
				"https://api.mangadex.org/manga/" + mangaObject.data.id + "/feed?translatedLanguage[]=en" +
				"&translatedLanguage[]=de&limit=300&includes[]=scanlation_group&includes[]=user&order[volume]=desc" +
				"&order[chapter]=desc&offset=0&contentRating[]=safe&contentRating[]=suggestive&contentRating" +
				"[]=erotica" + "&contentRating[]=pornographic", "GET");

		VBox back = new VBox();

		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader      inputReader      =
					new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson                gson             = new Gson();
			APIChaptersResponse chaptersResponse = gson.fromJson(inputReader, APIChaptersResponse.class);

			for(APIChaptersData chapter : chaptersResponse.data) {
				HBox hBox         = new HBox();
				Text chapterTitle = new Text("Ch." + chapter.attributes.chapter + chapter.attributes.title);

				Text group = null, user = null;
				for(APICChaptersRelationships relation : chapter.relationships) {
					if(relation.type.equals("scanlation_group"))
						group = new Text(relation.attributes.name);
					if(relation.type.equals("user"))
						user = new Text(relation.attributes.username);
				}

				Text createdAt = new Text(chapter.attributes.createdAt);

				hBox.getChildren().addAll(chapterTitle, group, user, createdAt);
				hBox.onMouseClickedProperty();//TODO Open Chapter
				back.getChildren().add(hBox);
			}
		}
		return back;
	}

	private static APIManga getMangaObject(String id) throws IOException {
		APIManga          manga;
		HttpURLConnection connection = HTTP.getHttpResponse("https://api.mangadex.org/manga/" + id + "/", "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(inputReader, APIManga.class);
		}
		return null;
	}

	//Wandelt die API Kapitel Daten aus dem Singleton in die nutzbare Kapitel-Klasse um und gibt dies als Array zurück
	private static Chapter[] getLatestChapters() throws IOException {

		//Eine Kapitel-Liste mit den letztlich zurückgegebenen Daten
		List<Chapter> chapters     = new ArrayList<Chapter>() {};

		//Eine Liste mit den benötigten Bildern für die Manga Covers
		List<String>  neededCovers = new ArrayList<String>() {};

		//Geht alle rohen API Daten durch
		for(APIChapter chapter : mangaReaderSingleton.mangaListLatestUpdate.data) {

			Chapter newChapter = new Chapter();

			//Speichert die einfach verfügbaren daten Kapitel Name, Kapitel Nummer und wann es aktualisiert wurde
			newChapter.chapterTitle  = chapter.attributes.title;
			newChapter.chapterNumber = chapter.attributes.chapter;
			newChapter.updatedAgo    = chapter.attributes.readableAt;

			//Geht die Beziehungen des Kapitels durch
			for(APIChapterRelationships relation : chapter.relationships) {

				//Sucht die Gruppe raus die das Kapitel verfügbar gemacht hat
				if(relation.type.equals("scanlation_group"))
					newChapter.group = relation.attributes.name;

				//Sucht den zugehörigen Mangas aus
				else if(relation.type.equals("manga")) {

					//Da der Titel unter umständen Null sein kann, wird in diesem Fall der Titel "localisation" gesetzt,
					//ansonsten ganz normal auf den Titel.
					if(relation.attributes.title.en != null)
						newChapter.title = relation.attributes.title.en;
					else
						newChapter.title = "localisation";

					//Speichert die ID des Mangas um nach dem Cover suchen zu können
					neededCovers.add(relation.id);
					newChapter.id = relation.id;
				}

				//Sobald alle Daten gefunden wurden, wird die Schleife beendet
				if(newChapter.group != null && newChapter.title != null)
					break;
			}

			//Speichert das fertige Kapitel
			chapters.add(newChapter);
		}

		//Ein String Array wird erstellt das zunächst die Manga IDs und später auch die Bild IDs der Covers beinhaltet
		String[] coversArray = new String[neededCovers.size()];
		coversArray = neededCovers.toArray(coversArray);
		String[] covers = getMultipleMangaCovers(coversArray);

		//Speichert die fertigen Cover Bilder in die Mangas ab
		saveCoverImages(chapters, covers);

		// Speichert letztlich alle Mangas in eine letzte Liste ab die ein Cover haben
		List<Chapter> finalChapter = new ArrayList<>();
		for(Chapter chapter : chapters) {
			if(chapter.mangaCover != null)
				finalChapter.add(chapter);
		}

		//Gibt die Liste mit Mangas als Array zurück
		Chapter[] chaptersArray = new Chapter[finalChapter.size()];
		return finalChapter.toArray(chaptersArray);
	}

	private static void saveCoverImages(List<Chapter> chapters, String[] covers) {
		//Die Standartadresse auf die für die Covers zugegriffen werden muss
		String baseHttpUrl = "https://uploads.mangadex.org/covers/";

		//Da die covers in der falschen Reihenfolge gespeichert werden sie in die richtigen Mangas eingefügt.
		//Anstatt die Strings einzuspeichern werden hier direkt die fertigen Bilder eingespeichert.
		for(Chapter chapter : chapters) {
			for(String id : covers) {
				if(id != null)
					if(chapter.id.equals(id.split("/")[0]))
						chapter.mangaCover = new Image(baseHttpUrl + id);
			}
		}
	}

	private static String[] getMultipleMangaCovers(String[] ids) throws IOException {

		//Bekommt den Bildpfad der Covers als Sting Array
		ids = getCoverArtFileNames(ids);

		//Überprüft alle Pfade
		for(int i = 0; i < ids.length; i++) {

			//Speichert nichts ab, wenn kein Bild vorhanden ist (Bilder haben immer "512.jpg")
			if(!ids[i].contains("512.jpg"))
				ids[i] = null;
		}

		return ids;
	}

	private static String[] getCoverArtFileNames(String[] ids) throws IOException {

		//Der Anfang an eine bestimmte URL die statisch ist
		String baseHttpUrl = "https://api.mangadex.org/manga?includes[]=cover_art&order[followedCount]=desc";

		//Fügt der Grund URL die IDs der gewünschten Mangas hinzu
		for(String id : ids) {
			baseHttpUrl += "&ids[]=" + id;
		}
		//Optionales wie ein Limit und welche art von Content ein/ausgefiltert werden soll
		baseHttpUrl += "&limit=" + ids.length + "&contentRating[]=safe";

		//Schickt die Http Abfrage ab und überprüft, ob diese erfolgreich ist
		HttpURLConnection connection = HTTP.getHttpResponse(baseHttpUrl, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

			//Wandelt den erhaltenen JSON Text in ein nutzbares Objekt um
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			CoverAbfrage   mangaArray  = gson.fromJson(inputReader, CoverAbfrage.class);

			//Ändert die größe des Arrays da durch Filterung manche Mangas nicht angezeigt werden
			//ids = new String[mangaArray.total];

			int i = 0;
			//Geht über jeden einzelnen Manga
			for(int j = 0; j < ids.length; j++) {
				for(CoverAbfrageData data : mangaArray.data) {
					if(data.id.equals(ids[j])) {
						//Sucht nach dem gewünschten Cover_art Relationship
						for(CoverAbfrageDataRelationships relationshipsAttributes : data.relationships) {
							if(relationshipsAttributes.type.equals("cover_art")) {
								//Das Array wird mit den ids der Mangas und dessen Dateinamen bestückt
								ids[j] += "/" + relationshipsAttributes.attributes.fileName + ".512.jpg";
							}
						}
						i++;
					}
				}
			}
			return ids;
		}

		//Wenn die Anfrage fehlgeschlagen ist, wird ein leeres Array zurückgegeben
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
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(inputReader, APIChapterListResponse.class);
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