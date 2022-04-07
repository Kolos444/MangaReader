import APIClasses.APILoginUser;
import com.google.gson.Gson;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;

import APIClasses.*;


public class LoginGUI {
	
	private final Stage         primaryStage;
	private       TextField     textFieldUsername;
	private       PasswordField passwordFieldPassword;
	private       Text          statusText;
	MangaReaderSingleton mangaReaderSingleton = MangaReaderSingleton.instance();
	SetStages            setStages;
	
	public Stage returnStage() {
		return primaryStage;
	}
	
	public LoginGUI(SetStages setStages) throws Exception {
		this.setStages = setStages;
		primaryStage   = new Stage();
		
		BorderPane LoginWindow = new BorderPane();
		
		LoginWindow.setTop(buildLoginHead());
		LoginWindow.setCenter(buildLoginForm());
		LoginWindow.setBottom(buildLoginButton());
		
		primaryStage.setScene(new Scene(LoginWindow));
		primaryStage.setTitle("Login");
		primaryStage.show();
	}
	
	private Node buildLoginHead() {
		VBox vBox       = new VBox();
		Text loginTitle = new Text("Login");
		statusText = new Text("Ich bin eine Status anzeige");
		vBox.getChildren().addAll(loginTitle, statusText);
		return vBox;
	}
	
	private Node buildLoginForm() {
		VBox vBox = new VBox();
		
		vBox.getChildren().addAll(buildLoginTextFieldUsername(), buildLoginPasswordFieldPassword());
		
		return vBox;
	}
	
	private Node buildLoginTextFieldUsername() {
		textFieldUsername = new TextField();
		return textFieldUsername;
	}
	
	private Node buildLoginPasswordFieldPassword() {
		passwordFieldPassword = new PasswordField();
		return passwordFieldPassword;
	}
	
	private Node buildLoginButton() {
		Button buttonLogin = new Button("Login");
		buttonLogin.setOnAction(ev -> {BuildHttpConnectionPostLogin();});
		return buttonLogin;
	}
	
	private String buildJsonObjectUser() {
		Gson gson = new Gson();
		mangaReaderSingleton.apiLoginUser = new APILoginUser(textFieldUsername.getCharacters().toString(),
															 passwordFieldPassword.getCharacters().toString());
		return gson.toJson(mangaReaderSingleton.apiLoginUser);
	}
	
	private void BuildHttpConnectionPostLogin() {
		try {
			HttpURLConnection connection =
					HTTP.getHttpResponse("https://api.mangadex.org/auth/login", buildJsonObjectUser(), "POST");
			
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader   inputReader   = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				Gson             gson          = new Gson();
				APILoginResponse loginResponse = gson.fromJson(inputReader, APILoginResponse.class);
				mangaReaderSingleton.apiToken = loginResponse.getToken();
				
				statusText.setText(loginResponse.getResult());
				
				//TODO Versuch nochmal ob es mit dem BufferedReader geht, auch wenn es sich wie Krebs anfühlt
				String json = gson.toJson(loginResponse);
				
				writeFile(json, "data.json");
				
				primaryStage.close();
				setStages.endLoginGUI();
			} else {
				statusText.setText("HTTP Error Code: " + connection.getResponseCode());
			}
		} catch(IOException e) {
			e.printStackTrace();
			statusText.setText("Error IO Exception");
		}
	}
	
	private void writeFile(String output, String fileName) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
			bufferedWriter.write(output);
			bufferedWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	

	

	

}