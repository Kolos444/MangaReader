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

public class MainGUI {
	SetStages setStages;
	private final Stage primaryStage;
	
	public Stage returnStage() {
		return primaryStage;
	}
	
	public MainGUI(SetStages setStages) {
		this.setStages = setStages;
		
		primaryStage = new Stage();
		primaryStage.initStyle(StageStyle.UNDECORATED);
		
		BorderPane mainWindow = new BorderPane();
		
		mainWindow.setTop(buildMainWindowTop());
		mainWindow.setCenter(buildMainWindowCenter());
		
		Scene scene = new Scene(mainWindow);
		scene.getStylesheets().addAll(MainGUI.class.getResource("mainWindow.css").toExternalForm(),
									  MainGUI.class.getResource("mainMenu.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.show();
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
		menu.getItems().addAll(buildMangaStandard(),buildMangaNewest(),buildMangaBest(),buildMangaPopular());
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
	
	private Node buildMainWindowCenter() {
		BorderPane mainBorder = new BorderPane();
		
		mainBorder.setMinHeight(680);
		mainBorder.setMinWidth(1280);
		
		//mainBorder.setCenter();
		
		return mainBorder;
	}
	

}
