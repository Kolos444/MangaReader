import APIMangaClasses.APIMangaListData;
import APIMangaClasses.APIMangaListResponse;
import com.google.gson.JsonObject;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;

public class SearchPage {

	static ScrollPane searchCore = new ScrollPane();

	public static ScrollPane searchManga(String text) throws IOException {

		//Es gibt einen Fehler bei vielen Suchen bei dem aus irgendeinem grund der Errorcode 400 ausgespuckt wird,
		// dies muss wohl an Java liegen da ich beispielsweise im Browser ohne problem eine Antwort bekomme. Leider
		// ist mein Gehirn viel zu klein um in den nächsten 48 Stunden herauszufinden warum dies der Fall ist.
		APIMangaListResponse mangaList = HomePage.getMangas(
				"https://api.mangadex.org/manga?limit=16&offset=0&includes[]=cover_art&includes[]=author" +
				"&includes[]=artist&contentRating[]=safe&contentRating[]=suggestive&title=" +
				text+ "&order[relevance]=desc");

		StringBuilder statisticsUrl = new StringBuilder("https://api.mangadex.org/statistics/manga?");
		assert mangaList != null;
		for(APIMangaListData manga : mangaList.data) {
			statisticsUrl.append("manga[]=").append(manga.id).append("&");
		}

		APIStatisticsResponse response = HomePage.getStatistics(statisticsUrl.toString());
		for(APIMangaListData manga : mangaList.data) {
			assert response != null;
			JsonObject jsonObject = response.statistics.get(manga.id).getAsJsonObject();
			manga.attributes.follows = jsonObject.get("follows").getAsInt();
			if(!jsonObject.get("rating").getAsJsonObject().get("average").isJsonNull())
				manga.attributes.rating = jsonObject.get("rating").getAsJsonObject().get("average").getAsDouble();
		}

		for(int j = 0; j < mangaList.data.length; j++) {
			for(int i = 0; i < mangaList.data[j].relationships.length; i++) {
				if(mangaList.data[j].relationships[i].type.equals("cover_art")) {
					mangaList.data[j].attributes.cover = new Image(
							"https://uploads.mangadex.org/covers/" + mangaList.data[j].id + "/" +
							mangaList.data[j].relationships[i].attributes.fileName);
				}
			}
		}

		VBox            searchContainer = new VBox();
		ArrayList<Node> mangas          = new ArrayList<>();
		for(APIMangaListData manga : mangaList.data) {
			mangas.add(buildSearchNode(manga));
		}

		for(int i = 0; i < mangas.size(); i += 2) {
			HBox row = new HBox(mangas.get(i));
			row.setAlignment(Pos.CENTER_RIGHT);

			if(i + 1 < mangas.size())
				row.getChildren().add(mangas.get(i + 1));

			searchContainer.getChildren().add(row);
		}

		searchCore.setContent(searchContainer);
		searchCore.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		searchCore.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		searchCore.setMaxWidth(RootNode.getWidth() - 1);
		return searchCore;
	}

	public static HBox buildSearchNode(APIMangaListData manga) {

		Rectangle cover =
				new Rectangle(150, manga.attributes.cover.getHeight() * 150 / manga.attributes.cover.getWidth(),
							  new ImagePattern(manga.attributes.cover));
		cover.getStyleClass().add("searchCover");

		Label title = new Label("No Title found");
		title.getStyleClass().add("searchTitle");
		if(manga.attributes.title.en != null)
			title.setText(manga.attributes.title.en);
		else if(manga.attributes.title.ja != null)
			title.setText(manga.attributes.title.ja);
		else if(manga.attributes.title.de != null)
			title.setText(manga.attributes.title.de);

		Label rating  = new Label("Rating: " + manga.attributes.rating);
		Label follows = new Label(" Follows: " + manga.attributes.follows);

		/*
		Von MangaDex noch nicht implementiert
		Label views             = new Label("N/A");
		Label comments          = new Label("N/A");
		*/

		Label publicationStatus = new Label(" Status: " + manga.attributes.status);

		HBox stats = new HBox(rating, follows/*, views, comments*/, publicationStatus);
		stats.getStyleClass().add("searchStats");

		HBox tags = MangaPage.getTags(manga.attributes);

		Label description = new Label("Not available");
		description.getStyleClass().add("searchDescription");
		if(manga.attributes.description.en != null)
			description = new Label(manga.attributes.description.en);
		if(manga.attributes.description.de != null)
			description = new Label(manga.attributes.description.de);

		VBox information = new VBox(title, stats, tags, description);
		information.getStyleClass().add("searchInfo");

		HBox core = new HBox(cover, information);
		core.getStyleClass().add("searchBox");
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
}