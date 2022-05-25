import APIChapters.APICChaptersRelationships;
import APIChapters.APIChaptersData;
import APIChapters.APIChaptersResponse;
import APIMangaClasses.APIManga;
import APIMangaClasses.APIMangaListRelationships;
import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class MangaPage {

	private static APIManga   mangaObject;
	private static ScrollPane mangaPage;

	public static ScrollPane getMangaPage() {
		return mangaPage;
	}

	public static void setMangaPage(ScrollPane mangaPage) {
		MangaPage.mangaPage = mangaPage;
	}

	public MangaPage() throws IOException {
		setManga("6b958848-c885-4735-9201-12ee77abcb3c");
	}

	static void setManga(String id) throws IOException {

		//Fals der gewünschte Manga schon geladen ist wird direkt returned
		if(mangaObject != null)
			if(id.equals(mangaObject.data.id))
				return;

		mangaObject = getMangaObject(id);
		VBox mangaPage = new VBox();
		mangaPage.getChildren().addAll(buildMangaTop(), buildMangaChapters());
		MangaPage.mangaPage = new ScrollPane(mangaPage);
	}

	public static Node buildMangaTop() {
		return buildMangaPresentation();
	}

	private static Node buildMangaPresentation() {

		//Das zurückgebende Element
		HBox back = new HBox();

		//Erstellt ein Rechteck in dem das Cover Bild angezeigt wird
		Rectangle cover = new Rectangle(250, 250);
		cover.setArcHeight(10.0f);
		cover.setArcWidth(10.0f);

		//Sucht das Cover Bild und speichert es im Rechteck
		for(APIMangaListRelationships relationship : mangaObject.data.relationships) {
			if(relationship.type.equals("cover_art")) {
				Image image = new Image("https://uploads.mangadex.org/covers/" + mangaObject.data.id + "/" +
										relationship.attributes.fileName);
				cover.setHeight(cover.getHeight() * image.getHeight() / image.getWidth());
				cover.setFill(new ImagePattern(image));
			}
		}

		//Erstellt ein LAbel des japanischen Titels der normalerweise der standard Titel ist
		Label title = new Label("No Title found");
		if(mangaObject.data.attributes.title.ja != null)
			title = new Label(mangaObject.data.attributes.title.ja);
		else if(mangaObject.data.attributes.title.en != null)
			title = new Label(mangaObject.data.attributes.title.en);
		title.getStyleClass().add("mangaTitle");

		//Erstellt ein Label des englischen Titels
		Label englishTitle = new Label("No english Title found");
		if(mangaObject.data.attributes.title.en != null) {
			englishTitle = new Label(mangaObject.data.attributes.title.en);
			englishTitle.setId("mangaEnglishTitle");
		}

		//Es gibt einen fehler bei dem Attributes speziell bei Author und Artist nicht initialisiert wird obwohl das
		// gleiche bei cover_art möglich ist
		/*
				//Sucht alle Autoren und Künstler raus und speichert sie in einem Label
				StringBuilder peopleString = new StringBuilder();
				for(APIMangaListRelationships relationship: mangaObject.data.relationships) {
					if(relationship.type.equals("author")||relationship.type.equals("artist"))
						if(relationship.attributes!=null)
							peopleString.append(relationship.attributes.name+", ");
				}
				Label people = new Label(peopleString.toString());*/


		back.getChildren().addAll(cover, new VBox(title, englishTitle/*,people*/, buildMangaDescription()));
		return back;
	}

	private static Node buildMangaDescription() {
		VBox back = new VBox();

		Label description = new Label(mangaObject.data.attributes.description.en);
		description.setWrapText(true);
		description.setMaxWidth(1000.0d);

		back.getChildren().add(description);

		return back;
	}

	public static APIManga getMangaObject(String id) throws IOException {
		HttpURLConnection connection =
				HTTP.getHttpResponse("https://api.mangadex.org/manga/" + id + "?includes[]=cover_art", "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(HomePage.filterGsonExceptions(inputReader), APIManga.class);
		}
		return null;
	}

	public static Node buildMangaChapters() throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(
				"https://api.mangadex.org/manga/" + mangaObject.data.id + "/feed?translatedLanguage[]=en" +
				"&translatedLanguage[]=de&limit=300&includes[]=scanlation_group&includes[]=user&order[volume]=desc" +
				"&order[chapter]=desc&offset=0&contentRating[]=safe&contentRating[]=suggestive&contentRating" +
				"[]=erotica" + "&contentRating[]=pornographic", "GET");

		VBox back = new VBox();

		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			APIChaptersResponse chaptersResponse =
					gson.fromJson(HomePage.filterGsonExceptions(inputReader), APIChaptersResponse.class);

			for(APIChaptersData chapter : chaptersResponse.data) {
				HBox hBox = new HBox();
				hBox.getStyleClass().add("chapterBox");
				hBox.setOnMouseClicked(event -> {
					try {
						ReadManga.open(chapter.id);
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				});

				Label chapterTitle = new Label("Ch." + chapter.attributes.chapter + " - " + chapter.attributes.title);
				chapterTitle.getStyleClass().addAll("chapterBoxContent");
				chapterTitle.getStyleClass().add("chapterBoxTitle");

				Label group = new Label("no group"), user = new Label("no uploader");
				group.getStyleClass().addAll("chapterBoxContent");
				user.getStyleClass().addAll("chapterBoxContent");

				for(APICChaptersRelationships relation : chapter.relationships) {
					if(relation.type.equals("scanlation_group"))
						group = new Label(relation.attributes.name);
					if(relation.type.equals("user"))
						user = new Label(relation.attributes.username);
				}

				Label createdAt = new Label(chapter.attributes.createdAt);
				createdAt.getStyleClass().add("chapterBoxUpdatedAgo");
				createdAt.getStyleClass().add("chapterBoxContent");


				hBox.getChildren().addAll(chapterTitle, group, user, createdAt);
				hBox.onMouseClickedProperty();//TODO Open Chapter
				HBox placeholder = new HBox();
				placeholder.setMinHeight(5.0d);
				back.getChildren().addAll(hBox, placeholder);
			}
		}
		return back;
	}
}
