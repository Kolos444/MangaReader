import javafx.application.Application;
import javafx.stage.Stage;

public class SetStages extends Application {

	//Die Kern-Stage in der alles dargestellt wird
	Stage primaryStage;

	//Die Haupt GUI in der so ziemlich alles stattfindet
	MainGUI mainGUI;
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		mainGUI = new MainGUI();
		displayMainGUI();
	}

	//Setzt die Main GUI als angezeigte in der primaryStage
	public void displayMainGUI(){
		primaryStage = mainGUI.returnStage();
	}
}
