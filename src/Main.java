/**
 * Entry for NarrativeContainer application
 * <p>
 * Starts a {@link MainController} and console inputs, prompts for MySQL credentials and displaying all text menus
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class Main {
    /**
     * Launches the console of Narrative Container
     *
     * @param args Command passed at start is unsused
     */
    public static void main(String[] args) {
        MainController controller = new MainController();
        controller.start();
    }

}
// Starts the MainController Loop Start