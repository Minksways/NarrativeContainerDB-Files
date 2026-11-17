import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JJUnit 5 Testing section for running automate failing and passing states to simulate database states and verify
 * both affirmative and negative pass requirements.
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class CaseTesting {

    /** Controller under test*/
    private MainController controller;
    /** List of {@link Characters} loaded before each test */
    private List<Characters> sampleDatabase;
    /** baselineSize tracks the total records inside your authentic project file
    to make sure tests work correctly. */
    private int baselineSize;

    /**
     * Starts a fresh run of {@link MainController} and loads the baseline each time before {@link Test} executes
     */
    @BeforeEach
    public void setUp() {
        controller = new MainController();

        // Reads from baselinr file
        sampleDatabase = controller.ReadAllFromFile("Characters.txt.txt");

        // Baseline size on disk to have a way to store the tests
        baselineSize = sampleDatabase.size();
    }
    /** NEGATIVE TESTING: Verifies that if an operator feeds a completely missing or
            bad path file name string, the code does not crash and outputs an empty array list. */
    // Read Operation
    @Test
    public void testLoadDatabase_NonExistentFile() {

        List<Characters> list = controller.ReadAllFromFile("MissingFileTest.txt");
        assertNotNull(list);
        assertEquals(0, list.size());
    }
    /** AFFIRMATIVE TESTING INPUT: Verifies that inserting a row successfully adds
            to memory array and increments score metric by  0.1 */
    // Create Operation
    @Test
    public void testAddCharacter_Success() {

        boolean result = controller.addCharacter(sampleDatabase, "9.9", true,
                "King's Landing", false, "Temporary Test Guard");
        assertTrue(result);
        assertEquals(baselineSize + 1, sampleDatabase.size());
    }

    /** NEGATIVE TESTING INPUT: Verifies boundary filters working as intended. */
    @Test
    public void testAddCharacter_Fail_MissingAttributes() {

        boolean result = controller.addCharacter(sampleDatabase, "", true,
                "", false, null);
        assertFalse(result);
        assertEquals(baselineSize, sampleDatabase.size());
    }
    /** AFFIRMATIVE TESTING INPUT: Generates temporary object to calculating
            correct size upon complete removal. */
    // Delete Operation
    @Test
    public void testDeleteCharacter_Success() {

        List<Characters> tempStorage = new ArrayList<>(sampleDatabase);
        tempStorage.add(new Characters("9999", false,
                "Braavos", true, "Temporary Drop Target"));
        int currentSize = tempStorage.size();

        boolean result = controller.deleteCharacter("9999", tempStorage);
        assertTrue(result);
        assertEquals(currentSize - 1, tempStorage.size());
    }
    /** NEGATIVE TESTING INPUT: Verifies that targeting an ID key that does
            not exist on disk is safely handled. */
    @Test
    public void testDeleteCharacter_Fail_NonExistentID() {

        boolean result = controller.deleteCharacter("FakeNonExistentIDString_999", sampleDatabase);
        assertFalse(result);
        assertEquals(baselineSize, sampleDatabase.size());
    }
    /** AFFIRMATIVE TESTING INPUT: Verifies that committing final review modifications routes
            the elements straight into an updated subversion elements such as 1.10 . */
    // Update Operation
    @Test
    public void testUpdateCharacter_Success() {

        boolean result = controller.updateCharacter(sampleDatabase, "1.9", "false",
                "Castle Black", "true", "Temporary Update Test Node");
        assertTrue(result);
        assertEquals(baselineSize + 1, sampleDatabase.size());
    }
    /** AFFIRMATIVE TESTING INPUT: Confirms that feeding an entirely unoccupied entry like 99
     * safely uses custom gap-filling rules if there is an available space it registers  */
    // Increment Engine & Batch Parsing
    @Test
    public void testDuplicateValidation_FreeSlot() {

        String ID = "9.3";
        String resolvedID = controller.IsDuplicate(ID, sampleDatabase);

            // VALIDATOR: Validates that the output matches a valid numeric sequence
            assertTrue(resolvedID.matches("^[0-9]+(\\.[0-9]+)?$"));

            // Double check that your engine didn't mutate or cut down your massive input string
            assertEquals(ID, resolvedID);
        }

    /** AFFIRMATIVE TESTING INPUT: Batch parser splits columns exactly at comma targets and prints a string entry. */
    @Test
    public void testParseBatchLine_Success() {

        boolean result = controller.parseBatchLine("99.7,true,Dorne,false,Martell Spearman", sampleDatabase);
        assertTrue(result);
    }
    /** NEGATIVE TESTING INPUT: Ensures structural line verify corrupted boundary data overloads. Can be missing or have extra */
    @Test
    public void testParseBatchLine_Fail_MalformedRow() {

        boolean result = controller.parseBatchLine("99.6,true,Testing,falseGuard ExtraCommaText", sampleDatabase);
        assertFalse(result);
    }
}
