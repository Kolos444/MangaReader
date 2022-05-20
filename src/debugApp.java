import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class debugApp extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		Stage readerStage = new Stage();
		//readerStage.initStyle(StageStyle.UNDECORATED);

		ImageView imageLeft = new ImageView("file:Images/Image not Found.jpg");
		imageLeft.setPreserveRatio(true);
		imageLeft.setFitHeight(1080.0d);
		ImageView imageRight = new ImageView("file:Images/Image not Found.jpg");
		imageRight.setPreserveRatio(true);
		imageRight.setFitHeight(1080.0d);

		BorderPane coreBorder = new BorderPane(new HBox(imageLeft, imageRight));
		//coreBorder.setRight(buildCloseButton());

		Scene scene = new Scene(new HBox(coreBorder));

		readerStage.setFullScreen(true);
		scene.setFill(Color.TOMATO);
		readerStage.setScene(scene);
		readerStage.show();
	}
}
