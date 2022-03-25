import java.net.HttpURLConnection;

public class MangaReaderSingleton {
	
	private static final MangaReaderSingleton holyInstance = new MangaReaderSingleton();
	
	private MangaReaderSingleton(){	}
	
	public static MangaReaderSingleton instance(){
		return holyInstance;
	}
	
}
