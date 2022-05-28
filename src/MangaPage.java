import APIChapters.APICChaptersRelationships;
import APIChapters.APIChaptersData;
import APIChapters.APIChaptersResponse;
import APIMangaClasses.*;
import com.google.gson.Gson;
import javafx.geometry.Insets;
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

	static void setManga(String id) throws IOException {

		//Falls der gewünschte Manga schon geladen ist, wird direkt returned
		if(mangaObject != null)
			if(id.equals(mangaObject.data.id))
				return;

		mangaObject = getMangaObject(id);
		VBox mangaPage = new VBox(buildMangaTop(), buildMangaChapters());
		MangaPage.mangaPage = new ScrollPane(mangaPage);
		MangaPage.mangaPage.setMaxWidth(RootNode.getWidth() - 1);
		MangaPage.mangaPage.setMinWidth(RootNode.getWidth() - 1);
		MangaPage.mangaPage.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		MangaPage.mangaPage.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
	}

	private static HBox buildMangaTop() {

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
		for(APIAltTitles altTitle : mangaObject.data.attributes.altTitles) {
			if(altTitle.en != null) {
				englishTitle = new Label(altTitle.en);
				englishTitle.getStyleClass().add("mangaEnglishTitle");
			}
		}

		back.getChildren()
			.addAll(cover, new VBox(title, englishTitle, getPeople(), getTags(mangaObject.data.attributes), buildMangaDescription()));
		return back;
	}

	private static Label getPeople() {
		//Sucht alle Autoren und Künstler raus und speichert sie in einem Label
		StringBuilder peopleString = new StringBuilder();
		for(APIMangaListRelationships relationship : mangaObject.data.relationships) {
			if(relationship.type.equals("author") || relationship.type.equals("artist"))
				if(relationship.attributes != null)
					peopleString.append(relationship.attributes.name).append(", ");
		}
		Label people = new Label(peopleString.toString());
		people.getStyleClass().add("mangaPeople");
		people.setPadding(new Insets(10, 0, 10, 0));
		return people;
	}

	public static HBox getTags(APIMangaListAttributes attributes) {

		HBox placeholder = new HBox();
		placeholder.setMinWidth(5);
		placeholder.setMaxWidth(5);

		Label contentRating = new Label();
		if(attributes.contentRating != null) {
			contentRating.setText(attributes.contentRating.toUpperCase());
			switch(attributes.contentRating) {
				case "Gore":
				case "Doujinshi":
					contentRating.setStyle("-fx-background-color :#FF4040;");
					break;
				case "Suggestive":
					contentRating.setStyle("-fx-background-color :#F79421;");
					break;
				default:
					contentRating.setStyle("-fx-background-color:#066000");
			}
			contentRating.getStyleClass().add("mangaTag");
		}
		HBox tags = new HBox(contentRating,placeholder);

		for(APITags tag : attributes.tags) {
			Label tagLabel = new Label(tag.attributes.name.en.toUpperCase());
			tagLabel.getStyleClass().add("mangaTag");

			placeholder = new HBox();
			placeholder.setMaxWidth(5);
			placeholder.setMinWidth(5);
			tags.getChildren().addAll(tagLabel, placeholder);
		}
		return tags;
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
		HttpURLConnection connection = HTTP.getHttpResponse(
				"https://api.mangadex.org/manga/" + id + "?includes[]=artist&includes[]=author&includes[]=cover_art",
				"GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson        = new Gson();
			return gson.fromJson(HomePage.filterGsonExceptions(inputReader), APIManga.class);
		}
		return null;
	}

	public static VBox buildMangaChapters() throws IOException {
		HttpURLConnection connection = HTTP.getHttpResponse(
				"https://api.mangadex.org/manga/" + mangaObject.data.id + "/feed?translatedLanguage[]=en" +
				"&translatedLanguage[]=de&limit=300&includes[]=scanlation_group&includes[]=user&order[volume]=desc" +
				"&order[chapter]=desc&offset=0&contentRating[]=safe&contentRating[]=suggestive", "GET");

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

				Label chapterTitle = new Label("Ch." + chapter.attributes.chapter + " - ");

				if(chapter.attributes.title != null)
					chapterTitle.setText(chapterTitle.getText() + chapter.attributes.title);

				chapterTitle.getStyleClass().addAll("chapterBoxContent");
				chapterTitle.getStyleClass().add("chapterBoxTitle");

				Label group = new Label("no group"), user = new Label("no uploader");


				for(APICChaptersRelationships relation : chapter.relationships) {
					if(relation.type.equals("scanlation_group"))
						group = new Label(relation.attributes.name);
					if(relation.type.equals("user"))
						user = new Label(relation.attributes.username);
				}
				group.getStyleClass().addAll("chapterBoxContent");
				user.getStyleClass().addAll("chapterBoxContent");


				Label createdAt = new Label(HomePage.getTimespan(chapter.attributes.createdAt));
				createdAt.getStyleClass().add("chapterBoxContent");
				createdAt.getStyleClass().add("chapterBoxUpdatedAgo");

				hBox.getChildren().addAll(chapterTitle, group, user, createdAt);
				HBox placeholder = new HBox();
				placeholder.setMinHeight(5.0d);
				back.getChildren().addAll(hBox, placeholder);
			}
		}
		return back;
	}
}
