import APIMangaClasses.APIMangaListData;
import APIMangaClasses.APIMangaListRelationships;
import APIMangaClasses.APIMangaListResponse;
import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Objects;

public class RootNode {

	//Die primaryStage in der alles angezeigt wird
	public static Stage stage;

	//Der Singleton mit dem Klassenübergreifend auf wichtige Objekte zugegriffen wird
	MangaReaderSingleton singleton = MangaReaderSingleton.instance();
	private static double xOffset, yOffset;
	private TextField searchBox;

	public Stage returnStage() {
		return stage;
	}

	public RootNode() throws IOException {

		stage = new Stage();
		stage.initStyle(StageStyle.UNDECORATED);

		singleton.rootNode = new BorderPane();

		//Ermöglicht das Bewegen des Fensters (https://stackoverflow.com/a/18177792/19121944)
		singleton.rootNode.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		singleton.rootNode.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});

		Scene scene = new Scene(singleton.rootNode);


		scene.getStylesheets()
			 .addAll(Objects.requireNonNull(getClass().getResource("CSS/MainWindow.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/NavBar.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/SeasonalManga.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/mangaPage.css")).toExternalForm());


		ReadManga.initializeReadManga();

		singleton.rootNode.setTop(buildMainWindowTop());
		singleton.rootNode.setCenter(buildMainWindowCenter());

		stage.setScene(scene);
		stage.show();
	}

	private Node buildMainWindowTop() throws IOException {

		BorderPane navigationBar = new BorderPane();

		navigationBar.setCenter(buildNavbarOptions());
		navigationBar.setLeft(buildNavbarLogo());
		navigationBar.setRight(buildMangaSearch());

		navigationBar.setMinWidth(1280);
		navigationBar.setMinHeight(40);
		navigationBar.setId("navBar");
		return navigationBar;
	}

	private Node buildNavbarOptions() throws IOException {
		HBox hBox = new HBox();
		hBox.getChildren().addAll(buildMangaMenu());
		return hBox;
	}

	private Node buildMangaMenu() throws IOException {

		//TODO Nachdem navBar erstellt wurde funktioniert setFill nicht mehr
		MenuBar navBar = new MenuBar();

		navBar.getMenus().addAll(getMenuManga(), getMenuFollowed(), getMenuRandom());
		navBar.setMinHeight(40);
		return navBar;
	}

	private Menu getMenuManga() {
		Menu menu = new Menu("Manga");
		menu.getItems().addAll(buildMangaStandard(), buildMangaNewest(), buildMangaBest(), buildMangaPopular());
		return menu;
	}

	private MenuItem buildMangaStandard() {
		MenuItem standard = new MenuItem("Manga");
		standard.setOnAction(event -> {
			singleton.centerViewNode.setCenter(HomePage.homeNode);
		});
		return standard;
	}

	private MenuItem buildMangaNewest() {
		MenuItem standard = new MenuItem("New");
		standard.setOnAction(event -> {});
		return standard;
	}

	private MenuItem buildMangaBest() {
		MenuItem standard = new MenuItem("Best");
		standard.setOnAction(event -> {});
		return standard;
	}

	private MenuItem buildMangaPopular() {
		MenuItem standard = new MenuItem("Popular");
		standard.setOnAction(event -> {});
		return standard;
	}

	private Menu getMenuFollowed() {
		Menu standard = new Menu("Followed");
		standard.setOnAction(event -> {});
		return standard;
	}

	private Menu getMenuRandom() {
		Menu standard = new Menu("Random");
		standard.setOnAction(event -> {});
		return standard;
	}

	private Node buildMangaSearch() {
		return new HBox(buildMangaSearchTextBox(), buildMangaSearchButton());
	}

	private Node buildMangaSearchTextBox() {
		searchBox = new TextField();
		return searchBox;
	}

	private Node buildMangaSearchButton() {
		Button button = new Button("Search");
		button.setOnAction(event -> {
			try {
				searchManga(searchBox.getText());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		button.setId("searchButton");
		return button;
	}

	private void searchManga(String text) throws IOException {
		APIMangaListResponse mangaList = HomePage.getMangas(
				"https://api.mangadex.org/manga?limit=100&offset=0&includes[]=cover_art&includes[]=author " +
				"&includes[]=artist&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&title =" +
				text + "&order[relevance]=desc");

		for(int j = 0; j < mangaList.data.length; j++) {
			for(int i = 0; i < mangaList.data[j].relationships.length; i++) {
				if(mangaList.data[j].relationships[i].type.equals("cover_art"))
					mangaList.data[j].attributes.cover = new javafx.scene.image.Image(
							"https://uploads.mangadex.org/covers/" + mangaList.data[j].id + "/" + mangaList.data[j].relationships[i].attributes.fileName);
			}
		}

		for(APIMangaListData manga : mangaList.data) {
			HomePage.buildSearchNode(manga);
		}
		return;
	}


	private void getSearchList(String text) throws IOException {
		String baseUrl = "https://api.mangadex.org/manga?limit=100&offset=0&includes[]=cover_art&includes" +
						 "[]=author&includes[]=artist&contentRating[]=safe&contentRating[]=suggestive" +
						 "&contentRating[]=erotica&title=" + text + "&order[relevance]=desc";
		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl + id, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader       reader    = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson                 gson      = new Gson();
			APIMangaListResponse mangaList = gson.fromJson(reader, APIMangaListResponse.class);
		}
	}

	private Node buildNavbarLogo() {
		//TODO Wie man Images einfügt nachschauen
		Image mangaDexLogo;
		return null;
	}

	private Node buildMainWindowCenter() throws IOException {

		BorderPane centerViewPane = new BorderPane();

		centerViewPane.setMinHeight(680);
		centerViewPane.setMaxHeight(680);
		centerViewPane.setMinWidth(1280);
		centerViewPane.setMaxWidth(1280);

		singleton.width  = centerViewPane.getMinWidth();
		singleton.height = centerViewPane.getMinHeight();

		HomePage.buildMainGUIMainPage();

		centerViewPane.setCenter(singleton.homePage);

		singleton.centerViewNode = centerViewPane;
		return centerViewPane;
	}


}
