import APIChapter.APIChapter;
import APIChapterHash.APIChapterHash;
import com.google.gson.Gson;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class ReadManga {

	private static Stage readerStage;

	public static void open(String id) throws IOException {
		readerStage.show();
		RootNode.stage.hide();
		openChapter(id);
	}

	public static void initializeReadManga() {
		readerStage = new Stage();
		readerStage.initStyle(StageStyle.UNDECORATED);
		//readerStage.setFullScreen(true);

		ImageView  imageLeft  = new ImageView("file:Images/Image not Found.jpg");
		ImageView  imageRight = new ImageView("file:Images/Image not Found.jpg");
		BorderPane coreBorder = new BorderPane(new HBox(imageLeft, imageRight));
		coreBorder.setRight(buildCloseButton());
		HBox root = new HBox(coreBorder);

		readerStage.setScene(new Scene(root));
	}

	private static Rectangle buildCloseButton() {
		Rectangle X = new Rectangle();

		Image image = new Image("file:Images/X.png");
		ImagePattern value = new ImagePattern(image);
		X.setFill(value);

		X.setOnMouseClicked(event -> closeReader());
		return X;
	}

	private static void closeReader() {
		RootNode.stage.show();
		readerStage.close();

	}

	private static void openChapter(String id) throws IOException {

		APIChapter apiChapter = getChapter(id);
		Image[] pages = getChapterPages(apiChapter);

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
		Image[] pages = new Image[apiChapter.data.attributes.pages];

		String baseUrl = "https://api.mangadex.org/at-home/server/";
		String forcePort = "?forcePort443=true";

		HttpURLConnection connection = HTTP.getHttpResponse(baseUrl+apiChapter.data.id+forcePort,"GET");
		if(connection.getResponseCode()==HttpURLConnection.HTTP_ACCEPTED){
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Gson gson = new Gson();
			apiChapterHash = gson.fromJson(reader, APIChapterHash.class);
		}else
			return (new Image[]{new Image("file:Images/Image not Found.jpg")});

		for(int i = 0; i < apiChapter.data.attributes.pages; i++) {
			connection = HTTP.getHttpResponse(apiChapterHash.baseUrl + apiChapterHash.chapter.data[i], "GET");
			if(connection.getResponseCode()==HttpURLConnection.HTTP_ACCEPTED)
				pages[i] = new Image(connection.getInputStream());
		}

		return new Image[0];
	}
}