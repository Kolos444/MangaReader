package APIClasses;

public class APILoginResponse {
	String   result;
	APIToken token;
	
	public APILoginResponse(String result, APIToken token) {
		this.result = result;
		this.token  = token;
	}
	
	public String getResult() {
		return result;
	}
	
	public APIToken getToken() {
		return token;
	}
}
