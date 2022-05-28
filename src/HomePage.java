import APIChapterClasses.APIChapter;
import APIChapterClasses.APIChapterListResponse;
import APIChapterClasses.APIChapterRelationships;
import APICustomListClasses.APISeasonalListResponse;
import APICustomListClasses.APISeasonalRelationships;
import APIMangaClasses.APIManga;
import APIMangaClasses.APIMangaListData;
import APIMangaClasses.APIMangaListResponse;
import CoverRequests.CoverRequests;
import CoverRequests.CoverRequestsData;
import CoverRequests.CoverRequestsDataRelationships;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class HomePage {

	//Singleton mit wichtigen Klassenübergreifenden Daten
	private static APIChapterListResponse  mangaListLatestUpdate;
	private static APIMangaListResponse    mangaListRecentlyAdded;
	private static APISeasonalListResponse mangaListSeasonalAdded;

	public static ScrollPane getHomeNode() {
		return homeNode;
	}

	public static void setHomeNode(ScrollPane homeNode) {
		HomePage.homeNode = homeNode;
	}

	private static ScrollPane homeNode;

	public static void buildMainGUIMainPage() throws IOException {

		//Updated die im Singleton Manga Listen mit den neusten, zuletzt aktualisierten und saisonal Mangas
		getMangaLists();

		//Gibt die Hauptanzeige der Anwendung zurück

		//TODO ab dieser Methode wird die setFillcolor der Fullscreen Scene ignoriert
		homeNode = getStartPage();

	}

	//Erstellt ung gibt die Hauptanzeige zurück
	public static ScrollPane getStartPage() throws IOException {

		VBox allItems = new VBox(getSeasonalList(), getLatestUpdate(), getRecentlyAdded());
		allItems.setMaxWidth(RootNode.getWidth());
		allItems.setAlignment(Pos.TOP_LEFT);

		ScrollPane scrollPane = new ScrollPane(allItems);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		return scrollPane;
	}

	private static VBox getSeasonalList() throws IOException {


		HBox       content      = buildSeasonalMangaListItem();
		ScrollPane seasonalList = new ScrollPane(content);
		seasonalList.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		seasonalList.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		seasonalList.setPrefViewportHeight(250.0d);

		HBox seasonalNode = buildButtons(content.getChildren().size(), seasonalList);
		seasonalNode.setAlignment(Pos.CENTER_LEFT);
		seasonalNode.setMaxWidth(RootNode.getWidth() - 1);

		Label seasonal_mangas = new Label("Seasonal Mangas");
		seasonal_mangas.getStyleClass().add("sectionTitle");
		seasonal_mangas.setPadding(new Insets(20, 0, 0, 0));

		return new VBox(seasonal_mangas, seasonalNode);
	}

	//Eine Liste der zuletzt hinzugefügten Kapitel
	private static VBox getLatestUpdate() throws IOException {

		//Besorgt die gewünschten Kapitel
		Chapter[] chapters = getLatestChapters();

		//Überschrift
		Label latestUpdatedSection = new Label("Latest Updates");
		latestUpdatedSection.getStyleClass().add("sectionTitle");
		HBox table = new HBox(latestUpdatedSection);

		VBox[] vBox = new VBox[4];

		for(int i = 0; i < 4; i++) {

			vBox[i] = new VBox();

			for(int a = 0; a < 6; a++)
				if(i * 6 + a < chapters.length)
					vBox[i].getChildren().add(buildChapterNode(chapters[i * 6 + a]));

			vBox[i].getStyleClass().add("latestUpdatedRow");
			table.getChildren().add(vBox[i]);
		}

		VBox core = new VBox(latestUpdatedSection, table);
		core.setAlignment(Pos.CENTER_LEFT);
		core.setMaxWidth(RootNode.getWidth());
		return core;
	}

	private static HBox buildChapterNode(Chapter chapterData) {

		Rectangle cover = new Rectangle(100.d, 100.d, 45.0d, 60.0d);
		cover.setArcHeight(8d);
		cover.setArcWidth(8d);

		if(chapterData.mangaCover != null)
			cover.setFill(new ImagePattern(chapterData.mangaCover));
		else
			cover.setFill(new ImagePattern(new Image("file:Images/Image not Found.jpg")));


		Label title = new Label("Not Found");
		if(chapterData.title != null)
			title.setText(chapterData.title);
		title.getStyleClass().add("latestUpdateTitle");

		Label chapter = new Label("Ch. " + chapterData.chapterNumber + " - " + chapterData.chapterTitle);
		chapter.getStyleClass().add("latestUpdateChapterTitle");

		Label group = new Label(chapterData.group);
		group.getStyleClass().add("latestUpdateGroup");

		String text = getTimespan(chapterData.updatedAgo);

		Label updatedAgo = new Label(text);
		updatedAgo.setAlignment(Pos.CENTER_RIGHT);
		updatedAgo.getStyleClass().add("latestUpdateUpdatedAgo");

		BorderPane groupTime = new BorderPane();
		groupTime.setLeft(group);
		groupTime.setRight(updatedAgo);

		VBox chapterInfo = new VBox(title, chapter, groupTime);
		chapterInfo.getStyleClass().add("chapterInfo");

		HBox updateBox = new HBox(cover, chapterInfo);
		updateBox.getStyleClass().add("updateBox");
		updateBox.setMinWidth(RootNode.getWidth() / 4 - 1);
		updateBox.setMaxWidth(RootNode.getWidth() / 4 - 1);

		groupTime.setMinWidth(updateBox.getMinWidth() - 45 - 3 - 17);
		groupTime.setMaxWidth(updateBox.getMinWidth() - 45 - 3 - 17);

		updateBox.setOnMouseClicked(event -> {
			try {
				MangaPage.setManga(chapterData.id);
				RootNode.getCenterNode().getChildren().set(0, MangaPage.getMangaPage());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});

		return updateBox;
	}

	public static String getTimespan(String chapterData) {
		String[] time  = chapterData.split("-", 3);
		String[] time2 = time[2].substring(3).split(":", 3);
		LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(time[0]), Month.of(Integer.parseInt(time[1])),
													   Integer.parseInt(time[2].substring(0, 2)),
													   Integer.parseInt(time2[0]), Integer.parseInt(time2[1]),
													   Integer.parseInt(time[2].substring(0, 2)));
		Duration duration = Duration.between(localDateTime, LocalDateTime.now(ZoneId.of("UTC")));

		String text;
		if(duration.toDays() >= 365) {
			text = ((int)duration.toDays() / 365) + "y " + (duration.toDays() - 365 * ((int)duration.toDays() / 365)) +
				   "d " + (duration.toHours() - duration.toDays() * 24) + "h " +
				   (duration.toMinutes() - duration.toHours() * 60) + "m ago" + " ago";
		} else if(duration.toDays() >= 1)
			text = duration.toDays() + "d " + (duration.toHours() - duration.toDays() * 24) + "h " +
				   (duration.toMinutes() - duration.toHours() * 60) + "m ago" + " ago";
		else if(duration.toHours() >= 1)
			text = duration.toHours() + "h " + (duration.toMinutes() - duration.toHours() * 60) + "m ago";
		else
			text = duration.toMinutes() + "m ago";
		return text;
	}

	/**
	 Wandelt die API Kapitel Daten aus dem Singleton in die nutzbare Kapitel-Klasse um und gibt dies als Array zurück

	 @return Gibt die zuletzt aktualisierten Kapitel in einem Chapter Array zurück
	 */
	private static Chapter[] getLatestChapters() throws IOException {

		//Eine Kapitel-Liste mit den letztlich zurückgegebenen Daten
		List<Chapter> chapters = new ArrayList<Chapter>() {};

		//Eine Liste mit den benötigten Bildern für die Manga Covers
		List<String> neededCovers = new ArrayList<String>() {};

		//Geht alle rohen API Daten durch
		for(APIChapter chapter : mangaListLatestUpdate.data) {

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

					//Da der Titel unter umständen Null sein kann, wird in diesem Fall der Titel "localisation"
					// gesetzt,
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

		//Gibt die Liste mit Mangas als Array zurück
		return chapters.toArray(new Chapter[0]);
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
		ArrayList<String> paths = new ArrayList<>();

		//Überprüft alle Pfade
		for(String id : ids) {

			//Speichert nichts ab, wenn kein Bild vorhanden ist (Bilder haben immer "512.jpg")
			if(id.contains("256.jpg"))
				paths.add(id);
		}

		return paths.toArray(new String[0]);
	}

	private static String[] getCoverArtFileNames(String[] ids) throws IOException {

		//Der Anfang an eine bestimmte URL die statisch ist
		StringBuilder baseHttpUrl =
				new StringBuilder("https://api.mangadex.org/manga?includes[]=cover_art&order[followedCount]=desc");

		//Fügt der Grund URL die IDs der gewünschten Mangas hinzu
		for(String id : ids) {
			baseHttpUrl.append("&ids[]=").append(id);
		}
		//Optionales wie ein Limit und welche art von Content ein/ausgefiltert werden soll
		baseHttpUrl.append("&limit=").append(ids.length)
				   .append("&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating")
				   .append("[]=pornographic");

		//Schickt die Http Abfrage ab und überprüft, ob diese erfolgreich ist
		HttpURLConnection connection = HTTP.getHttpResponse(baseHttpUrl.toString(), "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

			//Wandelt den erhaltenen JSON Text in ein nutzbares Objekt um
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			CoverRequests  mangaArray  = gson.fromJson(inputReader, CoverRequests.class);

			//Geht über jeden einzelnen Manga
			for(int i = 0; i < ids.length; i++) {
				for(CoverRequestsData data : mangaArray.data) {
					if(ids[i].equals(data.id)) {
						for(CoverRequestsDataRelationships relationship : data.relationships) {
							if(relationship.type.equals("cover_art")) {
								ids[i] += "/" + relationship.attributes.fileName + ".256.jpg";
								break;
							}
						}
						break;
					}
				}
			}

			return ids;
		}

		//Wenn die Anfrage fehlgeschlagen ist, wird ein leeres Array zurückgegeben
		return new String[0];
	}

	private static VBox getRecentlyAdded() throws IOException {
		HBox recentlyAdded = new HBox();

		ArrayList<String> neededCovers = new ArrayList<>();
		for(APIMangaListData manga : mangaListRecentlyAdded.data) {
			neededCovers.add(manga.id);
		}

		for(String mangaCover : getMultipleMangaCovers(neededCovers.toArray(new String[0]))) {
			for(APIMangaListData manga : mangaListRecentlyAdded.data) {
				String id = mangaCover.split("/")[0];
				if(id.equals(manga.id))
					manga.attributes.cover = new Image("https://mangadex.org/covers/" + mangaCover);
			}
		}

		for(APIMangaListData manga : mangaListRecentlyAdded.data) {
			recentlyAdded.getChildren().add(buildRecentlyAddedManga(manga));
		}

		ScrollPane scrollPane = new ScrollPane(recentlyAdded);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

		HBox hBox = buildButtons(recentlyAdded.getChildren().size(), scrollPane);
		hBox.setMaxWidth(RootNode.getWidth() - 1);
		Label recently_added = new Label("Recently Added");
		recently_added.getStyleClass().add("sectionTitle");
		return new VBox(recently_added, hBox);
	}

	private static HBox buildButtons(int children, ScrollPane scrollPane) {
		Button toLeft = new Button("<");
		toLeft.setMinHeight(150d);
		toLeft.getStyleClass().add("seasonalButton");

		Button toRight = new Button(">");
		toRight.setMinHeight(150d);
		toRight.getStyleClass().add("seasonalButton");

		toRight.setOnAction(event -> {
			double set = scrollPane.getHvalue() + 3d / children;

			scrollPane.setHvalue(Math.min(set, scrollPane.getHmax()));
		});

		toLeft.setOnAction(event -> {
			double set = scrollPane.getHvalue() - 3d / children;

			if(set < 0)
				scrollPane.setHvalue(0);
			else
				scrollPane.setHvalue(set);
		});

		return new HBox(toLeft, scrollPane, toRight);
	}

	private static VBox buildRecentlyAddedManga(APIMangaListData manga) {

		Rectangle cover = new Rectangle(128d, 180d);
		cover.setArcWidth(10d);
		cover.setArcHeight(10d);
		if(manga.attributes.cover != null)
			cover.setFill(new ImagePattern(manga.attributes.cover));
		else
			cover.setFill(new ImagePattern(new Image("file:Images/Image not Found.jpg")));

		Label title = new Label("Not Found");
		title.getStyleClass().add("recentlyAddedTitle");
		title.setMaxWidth(128d);
		if(manga.attributes.title.en != null)
			title.setText(manga.attributes.title.en);
		else if(manga.attributes.title.ja != null)
			title.setText(manga.attributes.title.ja);
		else if(manga.attributes.title.de != null)
			title.setText(manga.attributes.title.de);

		VBox core = new VBox(cover, title);
		core.getStyleClass().add("recentlyAddedCore");
		core.setOnMouseClicked(event -> {
			try {
				MangaPage.setManga(manga.id);
				RootNode.getCenterNode().getChildren().set(0, MangaPage.getMangaPage());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		return core;
	}

	private static void getMangaLists() throws IOException {

		mangaListLatestUpdate  = getChapters(
				"https://api.mangadex.org/chapter?includes[]=manga&includes[]=scanlation_group&limit=24&translated" +
				"Language[]=en&translatedLanguage[]=de&contentRating[]=safe&contentRating[]=suggestive&" +
				"contentRating[]=erotica&order[readableAt]=desc");
		mangaListRecentlyAdded = getMangas(
				"https://api.mangadex.org/manga?limit=20&contentRating[]=safe&contentRating[]=suggestive&" +
				"contentRating[]=erotica&order[createdAt]=desc&includes[]=cover_art");
		mangaListSeasonalAdded =
				getMangasCustomList("https://api.mangadex.org/list/1f43956d-9fe6-478e-9805-aa75ec0ac45e");


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

	static APIMangaListResponse getMangas(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(filterGsonExceptions(inputReader), APIMangaListResponse.class);
		}
		return new APIMangaListResponse();
	}

	//Korrigiert Fehler erzeugende JSON Dateien
	public static BufferedReader filterGsonExceptions(BufferedReader inputReader) throws IOException {

		//Speichert den Buffer als String zum Bearbeiten
		String tmp = inputReader.readLine();

		//Behebt einen Fehler bei dem eine Description ohne Daten als Array und nicht Objekt gesehen wird
		//Expected BEGIN_ARRAY but was BEGIN_OBJECT
		tmp = tmp.replace("\"description\":[]", "\"description\":{\"en\":\"No Description\"}");

		//Vermutlich noch brauchbar
		//tmp = tmp.replace("\"altNames\":[]", "\"altNames\":{\"en\":\"No Alternative Name\"}");

		//Speichert und gibt einen neuen Reader korrigiert zurück
		Reader inputString = new StringReader(tmp);
		return new BufferedReader(inputString);
	}

	private static APISeasonalListResponse getMangasCustomList(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(inputReader, APISeasonalListResponse.class);
		}
		return null;
	}

	private static HBox buildSeasonalMangaListItem() throws IOException {

		ArrayList<String>   neededCovers = new ArrayList<>();
		ArrayList<APIManga> mangas       = new ArrayList<>();

		for(APISeasonalRelationships relationship : mangaListSeasonalAdded.data.relationships) {

			APIManga manga = getManga(relationship.id);
			if(manga != null) {
				mangas.add(manga);
				neededCovers.add(relationship.id);
			}
		}

		String baseHttpUrl = "https://mangadex.org/covers/";
		for(String coverPath : getMultipleMangaCovers(neededCovers.toArray(new String[0]))) {
			for(APIManga manga : mangas) {
				if(manga.data.id.equals(coverPath.split("/")[0])) {
					manga.data.cover = new Image(baseHttpUrl + coverPath);
				}
			}
		}

		StringBuilder statisticsUrl = new StringBuilder("https://api.mangadex.org/statistics/manga?");
		for(APIManga manga : mangas) {
			statisticsUrl.append("manga[]=").append(manga.data.id).append("&");
		}

		APIStatisticsResponse response = getStatistics(statisticsUrl.toString());
		for(APIManga manga : mangas) {
			assert response != null;
			JsonObject jsonObject = response.statistics.get(manga.data.id).getAsJsonObject();
			manga.data.follows = jsonObject.get("follows").getAsInt();
			manga.data.rating  = jsonObject.get("rating").getAsJsonObject().get("average").getAsDouble();
		}

		mangas.sort((o1, o2) -> Integer.compare(o2.data.follows, o1.data.follows));

		HBox hBox = new HBox();
		for(APIManga manga : mangas) {
			hBox.getChildren().add(buildSeasonalNode(manga));
		}

		return hBox;
	}

	static APIStatisticsResponse getStatistics(String url) throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(url, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			Gson           gson  = new Gson();
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			return gson.fromJson(input, APIStatisticsResponse.class);
		}
		return null;
	}

	private static HBox buildSeasonalNode(APIManga manga) {

		Image cover;
		if(manga.data.cover != null)
			cover = manga.data.cover;
		else
			cover = new Image("file:Images/Image not Found.jpg");

		PixelReader reader = cover.getPixelReader();
		double      width  = cover.getWidth();
		double      height = cover.getHeight();
		WritableImage newImage =
				new WritableImage(reader, (int)(width / 2 - 0.40789473 * height / 2), 0, (int)(0.40789473 * height),
								  (int)height);

		Rectangle seasonalCover = new Rectangle(93d, 230d, new ImagePattern(newImage));
		seasonalCover.getStyleClass().add("seasonalCovers");

		Label seasonalTitle = new Label("Unknown Title");
		seasonalTitle.setMaxWidth(230.0d);
		seasonalTitle.getStyleClass().add("seasonalTitle");
		if(manga.data.attributes.title.ja != null)
			seasonalTitle.setText(manga.data.attributes.title.ja);
		else
			seasonalTitle.setText(manga.data.attributes.title.en);

		Label seasonalDescription = new Label("No Description found");
		seasonalDescription.setWrapText(true);
		seasonalDescription.setMaxWidth(230.0d);
		seasonalDescription.setMaxHeight(200.0d);
		seasonalDescription.getStyleClass().add("seasonalDescription");
		if(manga.data.attributes.description.en != null)
			seasonalDescription.setText(manga.data.attributes.description.en);

		VBox seasonalText = new VBox(seasonalTitle, seasonalDescription);
		seasonalText.getStyleClass().add("seasonalTexts");

		HBox seasonalNode = new HBox(seasonalCover, seasonalText);
		seasonalNode.setMaxHeight(230d);
		seasonalNode.getStyleClass().add("seasonalNodes");
		seasonalNode.setOnMouseClicked(event -> {
			try {
				MangaPage.setManga(manga.data.id);
				RootNode.getCenterNode().getChildren().set(0, MangaPage.getMangaPage());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});

		return seasonalNode;
	}

	private static APIManga getManga(String id) throws IOException {
		String            baseUrl    = "https://api.mangadex.org/manga/";
		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl + id, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson   = new Gson();
			return gson.fromJson(reader, APIManga.class);
		}
		return null;
	}

}