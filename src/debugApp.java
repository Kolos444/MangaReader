import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class debugApp extends Application {
	private double xOffset,yOffset;
	private Stage  stage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = new Stage();
		stage.initStyle(StageStyle.UNDECORATED);

		stage.setScene(prepareScene(new HBox()));
		stage.show();
	}

	private Scene prepareScene(Node sourceNode) {
		BorderPane toolPane = new BorderPane();
		toolPane.setMinHeight(30.0d);
		toolPane.setStyle("-fx-background-color:#444444");
		toolPane.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		toolPane.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});

		BorderPane sceneCore = new BorderPane(sourceNode);
		sceneCore.setTop(toolPane);
		return new Scene(sceneCore);
	}

}
