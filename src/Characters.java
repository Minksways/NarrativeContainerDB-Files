/**
 * This is how a single Narrative Container Entry is formated for the insertions into
 * text files.
 * <p>
 * This Class is for the five different elements inside an entry these being: A unique identifier ID to be able to search
 * for it in a populated list, A true or false state for if the character lives or has died, a location marker for where
 * in the world of the story they can be found, another true or false for if the character has importance to plot or not,
 * and a very descriptive text of the character in this instance to allow for that characters backstory in full.
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */

public class Characters {
    /**
     * Unique ID number that can either be a 1 or 1.1 in the area containing entry
     */
    private String CharacterID;
    /**
     * Living or dead state of the charcter (t or f)
     */
    private boolean CurrentState;
    /**
     * A String for the location of the charcter
     */
    private String Location;
    /**
     * A importance identifier (t or f)
     */
    private boolean ImportanceState;
    /**
     * The big descriptive text section of the character
     */
    private String CharacterDescription;


    /**
     * Constructor of the entries
     *
     *
     * @param CharacterID           Unique ID
     * @param CurrentState          Living Status
     * @param Location              Location in story
     * @param ImportanceState       Plot importance
     * @param CharacterDescription  Character backstory
     */
    public Characters(String CharacterID, boolean CurrentState, String Location,
               boolean ImportanceState, String CharacterDescription) {
        this.CharacterID = CharacterID;
        this.CurrentState = CurrentState;
        this.Location = Location;
        this.ImportanceState = ImportanceState;
        this.CharacterDescription = CharacterDescription;
    }

    /**
     * Returns the Characters ID
     *
     * @return
     */
    // getter methods for the main controller of the project.
    public String getCharacterID()
    { return CharacterID; }

    /**
     * Returns the living Status
     *
     * @return
     */
    public boolean getCurrentState()
    { return CurrentState; }

    /**
     * Returns the Location of the Character
     *
     * @return
     */
    public String getLocation()
    { return Location; }

    /**
     * Returns plot importance
     *
     * @return
     */
    public boolean getImportanceState()
    { return ImportanceState; }

    /**
     * Returns the character's backstory text
     *
     * @return
     */
    public String getCharacterDescription()
    { return CharacterDescription; }
}
