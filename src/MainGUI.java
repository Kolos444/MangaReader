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

public class MainGUI {

	//Die primaryStage in der alles angezeigt wird
	private final Stage mainGUI;

	//Der Singleton mit dem Klassenübergreifend auf wichtige Objekte zugegriffen wird
	MangaReaderSingleton singleton = MangaReaderSingleton.instance();
	private static double xOffset, yOffset;

	public Stage returnStage() {
		return mainGUI;
	}

	public MainGUI() throws IOException {
		mainGUI = new Stage();
		mainGUI.initStyle(StageStyle.UNDECORATED);

		singleton.mainWindow = new BorderPane();

		//Ermöglicht das Bewegen des Fensters (https://stackoverflow.com/a/18177792/19121944)
		singleton.mainWindow.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		singleton.mainWindow.setOnMouseDragged(event -> {
			mainGUI.setX(event.getScreenX() - xOffset);
			mainGUI.setY(event.getScreenY() - yOffset);
		});

		Scene scene = new Scene(singleton.mainWindow);
		scene.getStylesheets().addAll(MainGUI.class.getResource("mainWindow.css").toExternalForm(),
									  MainGUI.class.getResource("mainMenu.css").toExternalForm());

		singleton.mainWindow.setTop(buildMainWindowTop());
		singleton.mainWindow.setCenter(buildMainWindowCenter());

		mainGUI.setScene(scene);
		mainGUI.show();
	}

	private Node buildMainWindowTop() {
		BorderPane navigationBar = new BorderPane();
		navigationBar.setCenter(buildNavbarOptions());
		navigationBar.setLeft(buildNavbarLogo());
		navigationBar.setRight(buildMangaSearch());

		navigationBar.setMinWidth(1280);
		navigationBar.setMinHeight(40);
		navigationBar.setId("navBar");
		return navigationBar;
	}

	private Node buildNavbarOptions() {
		HBox hBox = new HBox();
		hBox.getChildren().addAll(buildMangaMenu());
		return hBox;
	}

	private Node buildMangaMenu() {
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
		standard.setOnAction(event -> {});
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
		TextField textField = new TextField();
		return textField;
	}

	private Node buildMangaSearchButton() {
		Button button = new Button("Search");
		button.setOnAction(event -> {});
		button.setId("searchButton");
		return button;
	}

	private Node buildNavbarLogo() {
		//TODO Wie man Images einfügt nachschauen
		Image mangaDexLogo;
		return null;
	}

	private Node buildMainWindowCenter() throws IOException {
		BorderPane mainBorder = new BorderPane();

		mainBorder.setMinHeight(680);
		mainBorder.setMinWidth(1280);

		singleton.width  = mainBorder.getMinWidth();
		singleton.height = mainBorder.getMinHeight();

		mainBorder.setCenter(MainGUIMainPage.buildMainGUIMainPage());

		return mainBorder;
	}


}
