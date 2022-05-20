import APIChapter.APIChapter;
import APIChapterHash.APIChapterHash;
import com.google.gson.Gson;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ReadManga {

	private static Stage     readerStage;
	private static ImageView imageLeft;
	private static ImageView imageRight;
	private static Image[] pages;

	public static void open(String id) throws IOException {
		readerStage.show();
		RootNode.stage.hide();
		
		activateHotkeys();
		openChapter(id);
	}

	private static void activateHotkeys() {

	}

	public static void initializeReadManga() {
		readerStage = new Stage();
		readerStage.initStyle(StageStyle.UNDECORATED);

		imageLeft = new ImageView("file:Images/Image not Found.jpg");
		imageLeft.setPreserveRatio(true);
		imageLeft.setFitHeight(1080.0d);
		imageRight = new ImageView("file:Images/Image not Found.jpg");
		imageRight.setPreserveRatio(true);
		imageRight.setFitHeight(1080.0d);

		BorderPane coreBorder = new BorderPane(new HBox(imageLeft, imageRight));
		coreBorder.setRight(buildCloseButton());

		HBox hBox = new HBox(coreBorder);
		hBox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(hBox);

		//SetFill funktioniert völlig random, nach dem eine bestimme menuBar und VBox erstellt wurden, nicht mehr.
		scene.setFill(Color.BLACK);

		readerStage.setFullScreen(true);
		readerStage.setScene(scene);
	}

	private static Rectangle buildCloseButton() {
		Rectangle X = new Rectangle(20.0d, 20.0d);

		X.setFill(new ImagePattern(new Image("file:Images/X.png")));

		X.setOnMouseClicked(event -> closeReader());
		return X;
	}

	private static void closeReader() {
		RootNode.stage.show();
		readerStage.close();

	}

	private static void openChapter(String id) throws IOException {

		APIChapter apiChapter = getChapter(id);
		pages = getChapterPages(apiChapter);
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

	private static Image[] getChapterPages(APIChapter apiChapter) throws IOException {
		APIChapterHash apiChapterHash;
		Image[]        pages = new Image[apiChapter.data.attributes.pages];

		String baseUrl   = "https://api.mangadex.org/at-home/server/";
		String forcePort = "?forcePort443=true";

		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl + apiChapter.data.id + forcePort, "GET");
		if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson           gson   = new Gson();
			apiChapterHash = gson.fromJson(reader, APIChapterHash.class);
		} else
			return (new Image[]{new Image("file:Images/Image not Found.jpg")});

		for(int i = 0; i < apiChapter.data.attributes.pages; i++) {
			connection = HTTP.getHttpResponse(
					apiChapterHash.baseUrl + "/data/" + apiChapterHash.chapter.hash + '/' + apiChapterHash.chapter.data[i],
					"GET");
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
				pages[i] = new Image(connection.getInputStream());
		}

		return pages;
	}
}