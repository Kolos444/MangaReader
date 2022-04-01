import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class mainGUIMainPage {
	
	public static Node getStartPage() {
		VBox allItems = new VBox();
		allItems.getChildren().addAll(getSeasonalList(), getLatestUpdate(), getRecentlyAdded());
		return allItems;
	}
	
	
	private static HBox getSeasonalList() {
		HBox seasonalList = new HBox();
		return seasonalList;
	}
	
	private static HBox getLatestUpdate() {
		HBox latestUpdate = new HBox();
		return latestUpdate;
	}
	
	private static HBox getRecentlyAdded() {
		HBox recentlyAdded = new HBox();
		return recentlyAdded;
	}
}
