import APIChapter.APIChapter;
import APIChapterHash.APIChapterHash;
import com.google.gson.Gson;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Objects;

public class ReadManga {

	private static String imageBaseUrl, chapterHash;
	private static ImageView imageLeft, imageRight;
	private static int indexRight, indexLeft, loadedPages, maxPages;
	private static boolean  offset;
	private static Image[]  pages;
	private static String[] pagePath;
	private static Scene reader;

	public static void open(String id) throws IOException {

		RootNode.getStage().setScene(reader);
		RootNode.getStage().setFullScreen(true);

		openChapter(id);
	}

	public static void initializeReadManga() {

		prepareImageViews();

		reader = prepareScene();
	}

	private static Scene prepareScene() {
		BorderPane coreBorder = new BorderPane(new HBox(imageLeft, imageRight));
		coreBorder.setRight(buildCloseButton());

		HBox hBox = new HBox(coreBorder);
		hBox.setAlignment(Pos.CENTER);

		Scene scene = new Scene(hBox);
		scene.setOnKeyPressed(event -> {
			try {
				hotkey(event.getCode());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
		//Fill funktioniert völlig random, nach dem eine bestimme menuBar und VBox erstellt wurden, nicht mehr.
		scene.setFill(Color.BLACK);
		return scene;
	}

	private static void hotkey(KeyCode character) throws IOException {

		switch(character) {
			case LEFT:
				if(indexRight - 2 >= 0) {
					imageLeft.setVisible(true);

					imageLeft.setImage(pages[indexLeft - 2]);
					imageRight.setImage(pages[indexRight - 2]);

					indexLeft -= 2;
					indexRight -= 2;
				} else if(indexLeft - 2 >= 0) {
					imageRight.setVisible(false);

					imageLeft.setImage(pages[indexLeft - 2]);

					indexLeft -= 2;
					indexRight -= 2;
				}
				break;
			case RIGHT:
				if(indexLeft + 2 < maxPages) {
					imageRight.setVisible(true);

					imageLeft.setImage(pages[indexLeft + 2]);
					imageRight.setImage(pages[indexRight + 2]);

					indexLeft += 2;
					indexRight += 2;
				} else if(indexRight + 2 < maxPages) {
					imageLeft.setVisible(false);

					imageRight.setImage(pages[indexRight + 2]);

					indexLeft += 2;
					indexRight += 2;
				} else if(indexRight + 2 >= maxPages) {

					//TODO nächstes Kapitel öffnen
					//nextChapter();
				}

				//TODO Herausfinden warum die Bilder sich nicht aktualisieren bevor die folgende Methode ausgeführt
				// wird
				if(loadedPages < maxPages)
					loadPages(3);
				break;
			case O:
				if(!offset) {
					if(indexLeft + 1 < maxPages) {
						imageLeft.setVisible(true);

						imageLeft.setImage(pages[indexLeft + 1]);
						imageRight.setImage(pages[indexRight + 1]);

						indexLeft++;
						indexRight++;

						offset = true;
					} else if(indexRight + 1 < maxPages) {
						imageLeft.setVisible(false);

						imageRight.setImage(pages[indexRight + 1]);

						indexLeft++;
						indexRight++;

						offset = true;
					}
				} else {
					if(indexRight - 1 >= 0) {
						imageRight.setVisible(true);

						imageLeft.setImage(pages[indexLeft - 1]);
						imageRight.setImage(pages[indexRight - 1]);

						indexLeft--;
						indexRight--;

						offset = false;
					} else if(indexLeft - 1 >= 0) {
						imageRight.setVisible(false);

						imageLeft.setImage(pages[indexLeft - 1]);

						indexLeft--;
						indexRight--;

						offset = false;
					}
				}
				break;
		}
	}

	private static void prepareImageViews() {
		imageLeft  = new ImageView("file:Images/Image not Found.jpg");
		imageRight = new ImageView("file:Images/Image not Found.jpg");
		imageLeft.setPreserveRatio(true);
		imageRight.setPreserveRatio(true);
		imageLeft.setFitHeight(1080.0d);
		imageRight.setFitHeight(1080.0d);
	}

	private static Rectangle buildCloseButton() {

		Rectangle X = new Rectangle(20.0d, 20.0d);
		X.setFill(new ImagePattern(new Image("file:Images/X.png")));
		X.setOnMouseClicked(event -> closeReader());
		return X;
	}

	private static void closeReader() {

		RootNode.getStage().setScene(RootNode.getMainScene());
		RootNode.getStage().setFullScreen(false);
	}

	private static void openChapter(String id) throws IOException {

		getChapterPages(Objects.requireNonNull(getChapter(id)));

		indexRight = 0;
		indexLeft  = 1;

		if(pages.length > 1) {
			imageRight.setImage(pages[0]);
			imageLeft.setImage(pages[1]);
		} else if(pages.length == 1) {
			imageRight.setImage((pages[0]));
		}
	}


	private static APIChapter getChapter(String id) throws IOException {
		String            baseUrl    = "https://api.mangadex.org/chapter/";
		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl + id, "GET");

		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson   = new Gson();
			return gson.fromJson(reader, APIChapter.class);
		}
		return null;
	}

	private static void getChapterPages(APIChapter apiChapter) throws IOException {
		APIChapterHash apiChapterHash;
		pages = new Image[apiChapter.data.attributes.pages];

		String baseUrl   = "https://api.mangadex.org/at-home/server/";
		String forcePort = "?forcePort443=true";

		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl + apiChapter.data.id + forcePort, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson   = new Gson();
			apiChapterHash = gson.fromJson(reader, APIChapterHash.class);
		} else {
			return;
		}

		maxPages     = apiChapter.data.attributes.pages;
		imageBaseUrl = apiChapterHash.baseUrl;
		pagePath     = apiChapterHash.chapter.data;
		chapterHash  = apiChapterHash.chapter.hash;
		loadedPages  = 0;
		loadPages(4);
	}

	private static void loadPages(int i) throws IOException {
		HttpURLConnection connection;
		for(int startIndex = loadedPages; loadedPages < maxPages && loadedPages < startIndex + i; loadedPages++) {
			connection =
					HTTP.getHttpResponse(imageBaseUrl + "/data/" + chapterHash + '/' + pagePath[loadedPages], "GET");
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
				pages[loadedPages] = new Image(connection.getInputStream());
		}
	}
}