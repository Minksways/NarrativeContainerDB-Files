import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX GUI Application entry for Narrative Container dashboard
 * <p>
 *     Loads {@code view.fxml}, starts {@link MainController}, starts MySQL Connection start, resets fields to blank and displays window
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class MainApp extends Application {

    /**
     * Starts and displays JavaFX {@link Stage} for Narrative Container dashboard
     *
     * @param primaryStage  The primary stage provided at runtime
     * @throws Exception    If {@code View.fxml} could not be loaded ot found
     */
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

    /**
     * Launches the JavaFX application
     *
     * @param args command passed at start up
     */
    public static void main(String[] args) {
        launch(args);
    }
}
