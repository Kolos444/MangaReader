import javafx.scene.Node;
import javafx.stage.Stage;

public class ReadManga {
	public static void open(String id) {
		RootNode.stage.hide();

		Node viewer = openChapter(id);

		SetStages.primaryStage = new Stage();
	}

	private static Node openChapter(String id) {
		return null;
	}
}
