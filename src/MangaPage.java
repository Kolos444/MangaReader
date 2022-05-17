import APIChapters.APICChaptersRelationships;
import APIChapters.APIChaptersData;
import APIChapters.APIChaptersResponse;
import APIMangaClasses.APIManga;
import APIMangaClasses.APIMangaListRelationships;
import com.google.gson.Gson;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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

public class MangaPage {

	static void showManga(String id) throws IOException {
		GUIMainPage.mangaObject         = getMangaObject(id);
		GUIMainPage.singleton.mangaPage = new VBox();
		GUIMainPage.singleton.mangaPage.getChildren().addAll(buildMangaTop(), buildMangaChapters());
		GUIMainPage.singleton.mainWindow.setCenter(GUIMainPage.singleton.mangaPage);

	}

	public static Node buildMangaTop() {
		VBox back = new VBox();
		back.getChildren().addAll(buildMangaPresentation(), buildMangaDescription());
		return back;
	}

	private static Node buildMangaPresentation() {

		//Das zurückgebende Element
		HBox back = new HBox();

		//Erstellt ein Rechteck in dem das Cover Bild angezeigt wird
		Rectangle cover = new Rectangle(250, 250);
		cover.setArcHeight(10.0f);
		cover.setArcWidth(10.0f);

		//Sucht das Cover Bild und speichert es im Rechteck
		for(APIMangaListRelationships relationship : GUIMainPage.mangaObject.data.relationships) {
			if(relationship.type.equals("cover_art")) {
				Image image = new Image("https://uploads.mangadex.org/covers/" + GUIMainPage.mangaObject.data.id +
										"/" +
										relationship.attributes.fileName);
				cover.setHeight(cover.getHeight() * image.getHeight() / image.getWidth());
				if(image!=null)
					cover.setFill(new ImagePattern(image));
				else
					cover.setFill(new ImagePattern(new Image("../Images/Image not Found.jpg")));
			}
		}

		//Erstellt ein LAbel des japanischen Titels der normalerweise der standard Titel ist
		Label title = new Label("No Title found");
		title.setWrapText(true);
		if(GUIMainPage.mangaObject.data.attributes.title.ja != null)
			title = new Label(GUIMainPage.mangaObject.data.attributes.title.ja);
		else if(GUIMainPage.mangaObject.data.attributes.title.en != null)
			title = new Label(GUIMainPage.mangaObject.data.attributes.title.en);
		title.setId("mangaTitle");

		//Erstellt ein Label des englischen Titels
		Label englishTitle = new Label("No english Title found");
		if(GUIMainPage.mangaObject.data.attributes.title.en != null) {
			englishTitle = new Label(GUIMainPage.mangaObject.data.attributes.title.en);
			englishTitle.setId("mangaEnglishTitle");
		}

		//Es gibt einen fehler bei dem Attributes speziell bei Author und Artist nicht initialsiert wird obwohl das
		// gleiche bei cover_art möglich ist
		/*
				//Sucht alle Authoren und Künstler raus und speichert sie in einem Label
				StringBuilder peopleString = new StringBuilder();
				for(APIMangaListRelationships relationship: mangaObject.data.relationships) {
					if(relationship.type.equals("author")||relationship.type.equals("artist"))
						if(relationship.attributes!=null)
							peopleString.append(relationship.attributes.name+", ");
				}
				Label people = new Label(peopleString.toString());*/


		back.getChildren().addAll(cover, title, englishTitle/*,people*/);
		return back;
	}

	private static Node buildMangaDescription() {
		VBox back = new VBox();

		//javafx.scene.control.TextArea description =
		//TODO Kommenntar entfernen		new javafx.scene.control.TextArea(mangaObject.data.attributes.description.en);

		//back.getChildren().add(description);

		return back;
	}

	public static APIManga getMangaObject(String id) throws IOException {
		HttpURLConnection connection =
				HTTP.getHttpResponse("https://api.mangadex.org/manga/" + id + "?includes[]=cover_art", "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(inputReader, APIManga.class);
		}
		return null;
	}

	public static Node buildMangaChapters() throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(
				"https://api.mangadex.org/manga/" + GUIMainPage.mangaObject.data.id + "/feed?translatedLanguage[]=en" +
				"&translatedLanguage[]=de&limit=300&includes[]=scanlation_group&includes[]=user&order[volume]=desc" +
				"&order[chapter]=desc&offset=0&contentRating[]=safe&contentRating[]=suggestive&contentRating" +
				"[]=erotica" + "&contentRating[]=pornographic", "GET");

		VBox back = new VBox();

		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			APIChaptersResponse chaptersResponse =
					gson.fromJson(GUIMainPage.filterGsonExceptions(inputReader), APIChaptersResponse.class);

			for(APIChaptersData chapter : chaptersResponse.data) {
				HBox hBox         = new HBox();
				hBox.setId("chapterBox");

				Label chapterTitle = new Label("Ch." + chapter.attributes.chapter + " - " + chapter.attributes.title);
				chapterTitle.getStyleClass().addAll("chapterBoxContent");
				chapterTitle.setId("chapterBoxTitle");

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
				createdAt.setId("chapterBoxUpdatedAgo");
				createdAt.getStyleClass().addAll("chapterBoxContent");




				hBox.getChildren().addAll(chapterTitle, group, user, createdAt);
				hBox.onMouseClickedProperty();//TODO Open Chapter
				HBox placeholder = new HBox();
				placeholder.setMinHeight(5.0d);
				back.getChildren().addAll(hBox,placeholder);
			}
		}
		return back;
	}
}
