import APIClasses.APILoginUser;
import com.google.gson.Gson;
import javafx.application.Application;
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
import java.net.URL;
import java.util.stream.Collectors;

import APIClasses.*;


public class LoginGUI extends Application {

	private Stage         primaryStage;
	private TextField     textFieldUsername;
	private PasswordField passwordFieldPassword;
	private Text          statusText;
	MangaReaderSingleton mangaReaderSingleton = MangaReaderSingleton.instance();
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		
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
		buttonLogin.setOnAction(ev -> {buildLoginButtonOnAction();});
		return buttonLogin;
	}
	
	private void buildLoginButtonOnAction() {
		BuildHttpConnectionPostLogin();
	}
	
	private String buildJsonObjectUser() {
		Gson   gson              = new Gson();
		return gson.toJson(new APILoginUser(textFieldUsername.getCharacters().toString(), passwordFieldPassword.getCharacters().toString()));
	}
	
	private void BuildHttpConnectionPostLogin() {
		try {
			HttpURLConnection connection = getHttpPostResponse("https://api.mangadex.org/auth/login", buildJsonObjectUser());
			
			if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				Gson             gson = new Gson();
				APILoginResponse loginResponse = gson.fromJson(inputReader, APILoginResponse.class);
				
				statusText.setText(loginResponse.getResult());
				
				//TODO Versuch nochmal ob es mit dem BufferedReader geht auch wenn es sich wie Krebs anfühlt
				String json = gson.toJson(loginResponse);
				
				writeFile(json,"data.json");
				
			}else {
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
	
	private HttpURLConnection getHttpPostResponse(String url, String json) throws IOException {
		URL UrlObj = new URL(url);
		HttpURLConnection connection = getHttpPost(UrlObj);
		
		setHTTPPostBody(json, connection);
		
		return connection;
	}
	
	private void setHTTPPostBody(String json, HttpURLConnection connection) throws IOException {
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.writeBytes(json);
		outputStream.flush();
		outputStream.close();
	}
	
	private HttpURLConnection getHttpPost(URL UrlObj) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) UrlObj.openConnection();
		
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.setDoOutput(true);
		return connection;
	}
}