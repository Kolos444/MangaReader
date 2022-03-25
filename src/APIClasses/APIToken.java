package APIClasses;

public class APIToken {
	String session;
	String refresh;
	
	public APIToken(String session, String refresh) {
		this.session = session;
		this.refresh = refresh;
	}
	
	public String getSession() { return session; }
	
	public String getRefresh() {
		return refresh;
	}
}
