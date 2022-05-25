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
import java.io.IOException;
import java.util.Objects;

public class RootNode {

	private final        BorderPane rootNode;
	private static final double     width = 1280, height = 720;

	public static Stage getStage() {
		return stage;
	}

	public static void setStage(Stage stage) {
		RootNode.stage = stage;
	}

	public static double getWidth() {
		return width;
	}

	public static double getHeight() {
		return height;
	}

	public static HBox getCenterNode() {
		return centerNode;
	}

	//Die primaryStage in der alles angezeigt wird
	private static Stage stage;

	//Der Singleton mit dem Klassenübergreifend auf wichtige Objekte zugegriffen wird
	private static double xOffset, yOffset;
	private              TextField searchBox;
	private static final HBox      centerNode = new HBox();

	public RootNode() throws IOException {

		stage = new Stage();
		stage.initStyle(StageStyle.UNDECORATED);

		rootNode = new BorderPane();

		//Ermöglicht das Bewegen des Fensters (https://stackoverflow.com/a/18177792/19121944)
		rootNode.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		rootNode.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});

		Scene scene = new Scene(rootNode);


		scene.getStylesheets()
			 .addAll(Objects.requireNonNull(getClass().getResource("CSS/MainWindow.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/NavBar.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/SeasonalManga.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/mangaPage.css")).toExternalForm(),
					 Objects.requireNonNull(getClass().getResource("CSS/searchStyling.css")).toExternalForm());


		ReadManga.initializeReadManga();

		rootNode.setTop(buildMainWindowTop());
		rootNode.setCenter(buildMainWindowCenter());

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
			RootNode.getCenterNode().getChildren().set(0, HomePage.getHomeNode());
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
		searchBox.setOnAction(event -> {
			try {
				rootNode.setCenter(SearchPage.searchManga(searchBox.getText()));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		return searchBox;
	}

	private Node buildMangaSearchButton() {
		Button button = new Button("Search");
		button.setId("searchButton");
		button.setOnAction(event -> {
			try {
				rootNode.setCenter(SearchPage.searchManga(searchBox.getText()));
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		return button;
	}


	private Node buildNavbarLogo() {
		//TODO Wie man Images einfügt nachschauen
		Image mangaDexLogo;
		return null;
	}

	private Node buildMainWindowCenter() throws IOException {

		centerNode.setMinHeight(height - 40);
		centerNode.setMaxHeight(height - 40);
		centerNode.setMinWidth(width);
		centerNode.setMaxWidth(width);

		HomePage.buildMainGUIMainPage();
		centerNode.getChildren().add(HomePage.getHomeNode());


		return centerNode;
	}


}
