import javafx.application.Application;
import javafx.stage.Stage;

public class SetStages extends Application {
	
	Stage primaryStage;
	LoginGUI loginGUI;
	MainGUI mainGUI;
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
//		loginGUI = new LoginGUI(this);
		mainGUI = new MainGUI(this);
		
		//TODO Remember User
		
//		startLoginGUI();
		endLoginGUI();
	}
	
	private void startLoginGUI() {
		primaryStage = loginGUI.returnStage();
	}
	
	public void endLoginGUI(){
		primaryStage = mainGUI.returnStage();
	}
}
