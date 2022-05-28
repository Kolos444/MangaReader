import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTP {

	public static HttpURLConnection getHttpResponse(String url, String json, String method) throws IOException {
		URL               UrlObj     = new URL(url);
		HttpURLConnection connection = HTTP.createHttpConnection(UrlObj, method);

		HTTP.setHTTPBody(json, connection);

		return connection;
	}

	public static HttpURLConnection getHttpResponse(String url, String method) throws IOException {
		return HTTP.createHttpConnection(new URL(url), method);
	}
	
	private static HttpURLConnection createHttpConnection(URL UrlObj, String method) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) UrlObj.openConnection();
		
		connection.setRequestMethod(method);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0");
		connection.setDoOutput(true);
		return connection;
	}
	
	private static void setHTTPBody(String json, HttpURLConnection connection) throws IOException {
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.writeBytes(json);
		outputStream.flush();
		outputStream.close();
	}
	
}