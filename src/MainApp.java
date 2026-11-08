import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();


        controller.clearDashboardToBlank();

        primaryStage.setTitle("Narrative Container Dashboard");
        primaryStage.setScene(new Scene(root, 1000, 650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
