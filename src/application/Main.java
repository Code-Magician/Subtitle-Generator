package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	final String logoUrl = "https://w7.pngwing.com/pngs/305/696/png-transparent-closed-captioning-subtitle-television-show-video-miscellaneous-television-text.png";
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));

			Parent root = loader.load();
			
			Scene scene = new Scene(root);
			
			primaryStage.setTitle("Subtitles Generator");
			primaryStage.getIcons().add(new Image(logoUrl));
			primaryStage.setScene(scene);
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent arg0) {
					Platform.exit();
					System.exit(0);
				}
			});
	
			primaryStage.setMinHeight(500);
			primaryStage.setMinWidth(800);
			primaryStage.setResizable(false);
			
			primaryStage.show();
			
			
			Controller controller = loader.getController();
			controller.initialize(primaryStage);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
