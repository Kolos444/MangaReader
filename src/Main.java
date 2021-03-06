import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	//Die Kern-Stage in der alles dargestellt wird
	public static Stage primaryStage;

	//Die Haupt GUI in der so ziemlich alles stattfindet
	RootNode rootNode;

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		Main.primaryStage = primaryStage;
		rootNode          = new RootNode();
		displayMainGUI();
	}

	//Setzt die Main GUI als angezeigte in der primaryStage
	public void displayMainGUI(){
		primaryStage = RootNode.getStage();
	}
}