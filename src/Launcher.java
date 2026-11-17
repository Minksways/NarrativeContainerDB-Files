/**
 * Boot entry for packaging and launching the shaded JavaFX executable Jar
 * <p>
 * Moves the execution directly to {@link MainApp#main(String[])} without extending {@link javafx.application.Application}
 * , allowing unnamed module run via {@code java -jar}
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class Launcher {
    /**
     * Starts Application by forwarding commands to {@link MainApp}
     *
     * @param args Command parsed at start up
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
