/* These are all of our imports, including from file reading/writing, to user inputs,
arrays, arraysLists, and exception error handling
* */
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/** We have our beginning section here which is the main controller for both JavaFX GUI and Operations for the rest of
 * the project to change entries for Narrative Container.
 * <p>
 * This Class also accesses the user authentication and connection to the MySQL Database ({@code NarrativeContainerDB}),
 * Uses CRUD Operations to change entries information, a batch upload can be used or manual entry, text files and database
 * are synced with local CSV ({@code Characters.txt})
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class MainController {

    /**
     * Will Start a prompt scanner to get user login credentials for the MySQL Server Login and connect to the database,
     * ({@link DatabaseController}) then starting the interface for interacting with the Entries.
     * */
    private void initializeDatabaseConnection() {
        System.out.println("\n=========================================");
        System.out.println("===== MySQL Database Login Setup ========");
        System.out.println("=========================================");

        while (true) {
            System.out.println("Enter MySQL Server Address (e.g., localhost:3306 or 127.0.0.1)");
            System.out.print("Enter here: ");
            String serverAddress = scanner.nextLine().trim();

            System.out.print("Enter MySQL Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter MySQL Password: ");
            String password = scanner.nextLine().trim();

            this.dbController = new DatabaseController(serverAddress, username, password);

            if (dbController.testConnection()) {
                System.out.println("[SUCCESS] Connected to NarrativeContainerDB!\n");
                break;
            } else {
                System.out.println("[ERROR] Connection failed. Check your server address, " +
                        "username, or password and try again.\n");
            }
        }
    }

    /**
     * JavaFX for the FXML layout to open a window display GUI for changing information in the Database/Text Files. Loops
     * until valid credentials for MySQL login and connection are true.
     *
     */
    @FXML
    public void initialize() {
        while (true) {
            javafx.scene.control.Dialog<javafx.util.Pair<String, javafx.util.Pair<String,
                    String>>> loginDialog = new javafx.scene.control.Dialog<>();
            loginDialog.setTitle("MySQL Database Connection");
            loginDialog.setHeaderText("Enter your MySQL database credentials to connect:");

            javafx.scene.control.ButtonType connectButtonType =
                    new javafx.scene.control.ButtonType("Connect", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            loginDialog.getDialogPane().getButtonTypes().addAll(connectButtonType, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            javafx.scene.control.TextField hostField = new javafx.scene.control.TextField();
            hostField.setPromptText("e.g., localhost:3306");
            javafx.scene.control.TextField userField = new javafx.scene.control.TextField();
            userField.setPromptText("Username");
            javafx.scene.control.PasswordField passField = new javafx.scene.control.PasswordField();
            passField.setPromptText("Password");

            grid.add(new javafx.scene.control.Label("Server Address:"), 0, 0);
            grid.add(hostField, 1, 0);
            grid.add(new javafx.scene.control.Label("Username:"), 0, 1);
            grid.add(userField, 1, 1);
            grid.add(new javafx.scene.control.Label("Password:"), 0, 2);
            grid.add(passField, 1, 2);

            loginDialog.getDialogPane().setContent(grid);

            loginDialog.setResultConverter(dialogButton -> {
                if (dialogButton == connectButtonType) {
                    return new javafx.util.Pair<>(hostField.getText().trim(),
                            new javafx.util.Pair<>(userField.getText().trim(), passField.getText()));
                }
                return null;
            });

            java.util.Optional<javafx.util.Pair<String, javafx.util.Pair<String, String>>> result = loginDialog.showAndWait();

            if (!result.isPresent()) {
                javafx.application.Platform.exit();
                System.exit(0);
                return;
            }

            String host = result.get().getKey();
            String user = result.get().getValue().getKey();
            String pass = result.get().getValue().getValue();

            this.dbController = new DatabaseController(host, user, pass);

            if (dbController.testConnection()) {
                showAlert("Connected", "Successfully connected to NarrativeContainerDB!");
                break;
            } else {
                showAlert("Connection Failed", "Could not connect to MySQL. " +
                        "Check your server address, username, or password and try again.");
            }
        }
    }


    /** JavaFX Label for current loaded Character ID*/
    @FXML private Label guiIdDisplay;
    /** JavaFX Label for showing current selected if the character is alive or dead*/
    @FXML private Label guiStateLabel;
    /** JavaFX Label for showing the current location of the character*/
    @FXML private Label guiLocationLabel;
    /** JavaFX Label for showing the current importance of the character*/
    @FXML private Label guiImportanceLabel;
    /** JavaFX Label for showing the current characters backstory*/
    @FXML private Label guiDescriptionLabel;
    /** JavaFX text area for displaying a scrollable section of the backstory for if the section is lengthly*/
    @FXML private TextArea guiBackstoryArea;
    /** JavaFX input field for searching with a characters ID in the Database*/
    @FXML private TextField guiSearchIdInput;

    /**
     * Resets the first instance of opening the app to set up blank space placeholder of the Narrative container entries
     */
    public void clearDashboardToBlank() {
        this.activeCharacter = null;
        guiIdDisplay.setText("No Character Loaded");
        guiStateLabel.setText("---");
        guiLocationLabel.setText("---");
        guiImportanceLabel.setText("---");
        guiDescriptionLabel.setText("---");
        guiBackstoryArea.clear();
    }

    /**
     * Handles GUI ID search by using {@code guiSearchInput} against the current database to find the ID entered
     */
    @FXML
    private void handleSearchByID() {
        String searchID = guiSearchIdInput.getText().trim();
        if (searchID.isEmpty()) {
            showAlert("Input Error", "Please provide a valid Character Entry ID.");
            return;
        }

        List<Characters> allCharacters = ReadAllFromFile();
        Characters match = null;

        // Scan the file for exact ID match
        for (Characters ch : allCharacters) {
            if (ch.getCharacterID().equalsIgnoreCase(searchID)) {
                match = ch;
                break;
            }
        }

        // Found successfully will bind the data into the boxes, otherwise message
        if (match != null) {
            loadCharacterData(match);
        } else {
            showAlert("Record Not Found", "No character matching ID '" + searchID +
                    "' exists in storage.");
        }
    }


    /**
     * Displays JavaFX alert dialog ro communicate errors or successful updates
     *
     * @param title     The window displayed
     * @param message   The text shown to users
     */
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Loads al JavaFX Components with the chosen entry in each element {@link Characters} sets as current character
     *
     * @param ch The {@link Characters} display on dashboard, will ignore if {@code null}
     */
    public void loadCharacterData(Characters ch) {
        if (ch == null) return;
        this.activeCharacter = ch;

        // Bind character entry elements to the UI labels
        guiIdDisplay.setText("CharacterID: " + ch.getCharacterID());
        guiStateLabel.setText(ch.getCurrentState() ? "Alive: This character is alive, " +
                "add description." : "Deceased: This character is dead, add to description.");
        guiLocationLabel.setText(ch.getLocation());
        guiImportanceLabel.setText(ch.getImportanceState() ? "Important: " +
                "Must add important plot description." : "Not Important: Must add description.");
        guiDescriptionLabel.setText(ch.getCharacterDescription());
        guiBackstoryArea.setText(ch.getCharacterDescription());
    }

    /**
     * Updates a single attribute chosen of the current loaded character {@code activeCharacter} and refreshes the dashboard
     * before the new incremented entry is made.
     *
     * @param targetField   Allows for modifying ({@code "State"}, {@code "location"}, {code "importance"}, or {@code "description"}).
     * @param newValue      The updated value of the attribute.
     */
    private void WindowEditReadWrite(String targetField, String newValue) {
        if (activeCharacter == null) return;

        // Read parameters building of active screen memory
        boolean finalState = targetField.equals("state") ? Boolean.parseBoolean(newValue) : activeCharacter.getCurrentState();
        String finalLoc = targetField.equals("location") ? newValue : activeCharacter.getLocation();
        boolean finalImp = targetField.equals("importance") ? Boolean.parseBoolean(newValue) : activeCharacter.getImportanceState();
        String finalDesc = targetField.equals("description") ? newValue : activeCharacter.getCharacterDescription();

        //In memory
        this.activeCharacter = new Characters(activeCharacter.getCharacterID(), finalState,
                finalLoc, finalImp, finalDesc);

        // Update the dashboard panel text box live
        loadCharacterData(activeCharacter);
    }

    /**
     * JavaFX choice dialog Living state True t ({@code True} or False f {@code False}) of the current character.
     */
    @FXML
    private void handleEditState() {
        // Will only allow the + buttons ot work if there is information/ID opened
        if (activeCharacter == null) { showAlert("Error",
                "Input a character ID first."); return; }

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                activeCharacter.getCurrentState() ? "True" : "False", "True", "False"
        );
        dialog.setTitle("Edit CurrentState");
        dialog.setHeaderText("Change status for ID: " + activeCharacter.getCharacterID());
        dialog.setContentText("Select new value:");

        dialog.showAndWait().ifPresent(result -> {
            WindowEditReadWrite("state", result);
        });
    }

    /**
     * JavaFX text input to allow the location in the world
     */
    @FXML
    private void handleEditLocation() {
        if (activeCharacter == null) { showAlert("Error",
                "Input a character ID first."); return; }

        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(activeCharacter.getLocation());
        dialog.setTitle("Edit Location");
        dialog.setHeaderText("Modify active region coordinates:");
        dialog.setContentText("New Location String:");

        dialog.showAndWait().ifPresent(result -> {
            if (!result.trim().isEmpty()) {
                WindowEditReadWrite("location", result.trim());
            }
        });
    }

    /**
     * JavaFX choice dialog for Importance state True t ({@code True} or False f {@code False}) of the current character.
     */
    @FXML
    private void handleEditImportance() {
        if (activeCharacter == null) { showAlert("Error",
                "Input a character ID first."); return; }

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                activeCharacter.getImportanceState() ? "True" : "False", "True", "False"
        );
        dialog.setTitle("Edit ImportanceState");
        dialog.setHeaderText("Change the importance of the character ID");
        dialog.setContentText("Important to Narrative?:");

        dialog.showAndWait().ifPresent(result -> {
            WindowEditReadWrite("importance", result);
        });
    }

    /**
     * JavaFX text input area to allowing for changing the backstory of the character.
     */
    @FXML
    private void handleEditDescription() {
        if (activeCharacter == null) { showAlert("Error",
                "Input a character ID first."); return; }

        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Edit CharacterDescription");
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.TextArea editArea =
                new javafx.scene.control.TextArea(activeCharacter.getCharacterDescription());
        editArea.setWrapText(true);
        dialog.getDialogPane().setContent(editArea);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) return editArea.getText().trim();
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (!result.isEmpty()) {
                WindowEditReadWrite("description", result);
            }
        });
    }

    /**
     * Prompts for new ID number via a JavaFX dialog, validates it is new and will increment if it is an existing ID.
     */
    @FXML
    private void handleGuiCreateEntry() {
        //
        javafx.scene.control.TextInputDialog idPrompt = new javafx.scene.control.TextInputDialog();
        idPrompt.setTitle("Create New Character Profile");
        idPrompt.setHeaderText("Initialize a Brand New baseline Character record chain");
        idPrompt.setContentText("Enter New Base ID (e.g., 5 or 10):");

        idPrompt.showAndWait().ifPresent(inputID -> {
            String cleanedID = inputID.trim();

            //
            if (cleanedID.isEmpty()) {
                showAlert("Input Error", "ID cannot be empty. Entry aborted.");
                return;
            }
            if (!cleanedID.matches("^[0-9]+(\\.[0-9]+)?$")) {
                showAlert("Format Error", "Invalid ID structure. Numbers only please.");
                return;
            }

            //
            List<Characters> allCharacters = ReadAllFromFile();
            String verifiedID = IsDuplicate(cleanedID, allCharacters);

            if (!verifiedID.equals(cleanedID)) {
                showAlert("ID Exists", "ID '" + cleanedID +
                        "' is already in used, incrementing entry: " + verifiedID);
            }

            //
            this.activeCharacter = new Characters(
                    verifiedID,
                    true,
                    "Unassigned Location",
                    false,
                    "No description text added yet. " +
                            "Click '+' on elements to change."
            );

            //
            loadCharacterData(activeCharacter);
            showAlert("Entry made!", "ID '" + verifiedID +
                    "' successfully made, write and edit as desired to entry.");
        });
    }

    /**
     * Confirmation dialog, upon user confirmation will permanently delete the selected entry loaded in the window.
     */
    @FXML
    private void handleGuiDelete() {
        if (activeCharacter == null) {
            showAlert("Warning",
                    "No active character profile is currently loaded to delete.");
            return;
        }

        //
        javafx.scene.control.Alert confirmationDialog = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Are you completely sure you want to permanently delete Character ID '" +
                        activeCharacter.getCharacterID() + "'?",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO
        );
        confirmationDialog.setTitle("Deletion");
        confirmationDialog.setHeaderText("Entry will been permanently deleted.");


        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.YES) {
                List<Characters> allCharacters = ReadAllFromFile();

                boolean removed = deleteCharacter(activeCharacter.getCharacterID(), allCharacters);

                if (removed) {
                    System.out.println("[SUCCESS] Wiped record from storage: " +
                            activeCharacter.getCharacterID());

                    clearDashboardToBlank();

                    showAlert("Success",
                            "Character entry wiped cleanly from file storage.");
                }
            }
        });
    }

    /**
     * Commits the selected {@code activeCharacter} modifications as a new increment, updating database and text files.
     */
    @FXML
    private void handleCommitIncrement() {
        if (activeCharacter == null) {
            showAlert("Warning", "No character ID is currently selected.");
            return;
        }

        List<Characters> all = ReadAllFromFile();
        String nextVersionID = IsDuplicate(activeCharacter.getCharacterID(), all);

        Characters incrementedChar = new Characters(
                nextVersionID,
                activeCharacter.getCurrentState(),
                activeCharacter.getLocation(),
                activeCharacter.getImportanceState(),
                activeCharacter.getCharacterDescription()
        );

        all.add(incrementedChar);
        this.activeCharacter = incrementedChar;

        if (dbController != null) {
            dbController.insertCharacter(incrementedChar);
        }
        WriteAllToFile(all);
        loadCharacterData(activeCharacter);

        showAlert("Success", "All element changes made! Creating new increment: " + nextVersionID);
    }


    /**
     * Will show a read only version fo a selected character entry if that ID is valid.
     */
    @FXML
    private void handleGuiReadByEntry() {
        javafx.scene.control.TextInputDialog idPrompt = new javafx.scene.control.TextInputDialog();
        idPrompt.setTitle("ID Entry Lookup");
        idPrompt.setHeaderText("View ID Entry information");
        idPrompt.setContentText("Enter Character ID:");

        idPrompt.showAndWait().ifPresent(searchID -> {
            String targetID = searchID.trim();
            if (targetID.isEmpty()) {
                showAlert("Input Error", "Please provide a valid Character ID.");
                return;
            }

            List<Characters> allCharacters = ReadAllFromFile();
            Characters match = null;

            for (Characters ch : allCharacters) {
                if (ch.getCharacterID().equalsIgnoreCase(targetID)) {
                    match = ch;
                    break;
                }
            }

            if (match != null) {
                javafx.stage.Stage staticStage = new javafx.stage.Stage();
                staticStage.setTitle("Static Blueprint Record Reference - ID: " +
                        match.getCharacterID());

                javafx.scene.layout.VBox staticLayout = new javafx.scene.layout.VBox(12);
                staticLayout.setPadding(new javafx.geometry.Insets(20));
                staticLayout.setStyle("-fx-background-color: #F1F5F9;");

                javafx.scene.control.Label titleLabel =
                        new javafx.scene.control.Label("Character Entry section");
                titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1E293B;");

                javafx.scene.control.Label idLabel =
                        new javafx.scene.control.Label("Character ID:  " + match.getCharacterID());
                idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                javafx.scene.control.Label stateLabel =
                        new javafx.scene.control.Label("Current State:  " +
                        (match.getCurrentState() ? "True (Alive / Active)" :
                                "False (Deceased / Inactive)"));

                javafx.scene.control.Label locationLabel =
                        new javafx.scene.control.Label("Location:  " +
                        match.getLocation());

                javafx.scene.control.Label importanceLabel =
                        new javafx.scene.control.Label("Importance State:  " +
                        (match.getImportanceState() ? "True (High Narrative Priority)" :
                                "False (Standard Priority)"));

                javafx.scene.control.Label descriptionLabel =
                        new javafx.scene.control.Label("Character Description:");
                descriptionLabel.setStyle("-fx-font-weight: bold;");

                javafx.scene.control.TextArea descriptionText = new javafx.scene.control.TextArea(match.getCharacterDescription());
                descriptionText.setEditable(false);
                descriptionText.setWrapText(true);
                descriptionText.setPrefHeight(120);

                staticLayout.getChildren().addAll(
                        titleLabel,
                        new javafx.scene.control.Separator(),
                        idLabel,
                        stateLabel,
                        locationLabel,
                        importanceLabel,
                        descriptionLabel,
                        descriptionText
                );

                staticStage.setScene(new javafx.scene.Scene(staticLayout, 450, 400));
                staticStage.show();
            } else {

                showAlert("ID Not Found", "No character matching ID '" + targetID +
                        "' exists.");
            }
        });
    }


    /**
     * Opens a second scrollable JavaFX window showing all current entries available in the database.
     */
    @FXML
    private void handleGuiReadAll() {
        javafx.stage.Stage logStage = new javafx.stage.Stage();
        logStage.setTitle("Entries Log");


        javafx.scene.layout.VBox rowsContainer = new javafx.scene.layout.VBox(8);
        rowsContainer.setPadding(new javafx.geometry.Insets(15));
        rowsContainer.setStyle("-fx-background-color: #FFFFFF;");

        List<Characters> allCharacters = ReadAllFromFile();

        if (allCharacters.isEmpty()) {
            javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("The character " +
                    "text file storage database is currently empty.");
            emptyLabel.setStyle("-fx-text-fill: #64748B; -fx-font-style: italic;");
            rowsContainer.getChildren().add(emptyLabel);
        } else {

            for (Characters ch : allCharacters) {
                String rowText = String.format(
                        "ID: %s   |   State: %b   |   " +
                                "Location: %s   |   Importance: %b\nDescription: %s",
                        ch.getCharacterID(),
                        ch.getCurrentState(),
                        ch.getLocation(),
                        ch.getImportanceState(),
                        ch.getCharacterDescription()
                );

                javafx.scene.control.Label recordRowLabel = new javafx.scene.control.Label(rowText);
                recordRowLabel.setWrapText(true);

                recordRowLabel.setStyle("-fx-border-color: #E2E8F0; " +
                        "-fx-border-width: 0 0 1 0; -fx-padding: 10 5 10 5; " +
                        "-fx-text-fill: #334155; -fx-font-size: 13px;");
                recordRowLabel.setMaxWidth(Double.MAX_VALUE);

                rowsContainer.getChildren().add(recordRowLabel);
            }
        }


        javafx.scene.control.ScrollPane scrollPane =
                new javafx.scene.control.ScrollPane(rowsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 450);

        logStage.setScene(new javafx.scene.Scene(scrollPane));
        logStage.show();
    }

    /**
     * Asks for a text file path/file name via JavaFX input dialog (Characters_BatchTester.txt),
     * parses the CSV line into {@link Characters} and inserts them into the Database and text files.
     */
    @FXML
    private void handleGuiBatchUpload() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Batch text file upload");
        dialog.setHeaderText("Upload entries from text file path");
        dialog.setContentText("Enter text file name:");

        dialog.showAndWait().ifPresent(filePath -> {
            String sanitizedPath = filePath.replace("\"", "").trim();
            java.io.File targetFile = new java.io.File(sanitizedPath);

            if (!targetFile.exists() || !targetFile.isFile()) {
                showAlert("Import Aborted",
                        "The file specified could not be located or " +
                                "verified on this system.");
                return;
            }

            List<Characters> masterList = ReadAllFromFile();
            int DatabaseStorage = masterList.size();
            int successfulImports = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    if (parseBatchLine(line, masterList)) {
                        successfulImports++;
                    }
                }

                if (successfulImports > 0) {
                    if (dbController != null) {
                        for (int i = DatabaseStorage; i < masterList.size(); i++) {
                            dbController.insertCharacter(masterList.get(i));
                        }
                    }
                    WriteAllToFile(masterList);
                    showAlert("Batch Processing Complete",
                            "Successfully parsed, ordered, and committed " +
                                    successfulImports + " new records.");
                } else {
                    showAlert("Parsing Exception",
                            "Zero valid character rows could be parsed from " +
                                    "that target file context layout.");
                }
            } catch (IOException e) {
                showAlert("Exception Error", "Failed reading file metrics: " +
                        e.getMessage());
            }
        });
    }


    /** Tracks the {@link Characters} currently loaded. */
    private Characters activeCharacter;

    /** The text file use for storage and syncing across the project. */
    private final String TextFileName = "Characters.txt";

    /** Input scanner for reading user inputs and validating them. */
    private final Scanner scanner;

    /** Database Controller connection used to execute valid inputs into the MySQL from project. */
    private DatabaseController dbController;

    /** isRunning is used to keep a true state for the program running and displaying. */
    private boolean isRunning;
    /** IDExists keeps track if a current entry exists in the file to then increment it. */
    private boolean IDExists = false;


    /**
     * Makes a new {@code MainController} run , starts the scanner and main loop for active runtime.
     */
    public MainController() {
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
    }

    /** Main Menu display for all choices and submenu buttons. */
    private void DisplayMenu() {
        System.out.println("\n=========================================================");
        System.out.println("===== CRUD Operations Testing in IDE Console or CLI =====");
        System.out.println("=========================================================");
        System.out.println("\n1. Create Character Entries");
        System.out.println("2. Read Character Entries");
        System.out.println("3. Update/Delete Character Entries");
        System.out.println("4. Exit Program");
        System.out.println("\n=========================================================");
    }

    /**
     * User Choice Inputs Handler to have an area to type and go to next line.
     *
     * @return The numbers 1-4 choices outcomes or the default which loops {@code -1} if a non number was entered.
     */
    private int UserChoice() {
        System.out.print("Type here: ");
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return -1;
        }
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    /**
     * Changes submenus from main menu depending on CRUD method chosen.
     *
     * @param choice The numbers 1-4 choices outcomes or the default which loops {@code -1} if a non number was entered.
     */
    // Choices for Main Menu
    private void MenuChoiceOutcomes(int choice) {
        switch (choice) {
            case 1:
                SubMenuCreate();
                break;
            case 2:
                SubMenuRead();
                break;
            case 3:
                SubMenuUpdateDelete();
                break;
            case 4:
                // Exits the program
                System.out.println("\nExiting Program,,, ");
                this.isRunning = false;
                break;
            default:
                System.out.println("Invalid choice, try again (1-4)");
        }
    }


    /**
     * Sub Menu display for adding/creating an entry
     */
    private void SubMenuCreate() {
        boolean DisplaySubMenu = true;

        while (DisplaySubMenu) {
            System.out.println("\n==============================");
            System.out.println("===== Batching or Manual =====");
            System.out.println("==============================");
            System.out.println("1. Manual Create Character Entry");
            System.out.println("2. Batch Upload Character Entry");
            System.out.println("3. Go Back to Main Menu");
            System.out.println("4. Exit Program");
            System.out.println("\n==============================");

            // This submenu choices
            int SubMenuChoiceOutcomes = UserChoice();

            switch (SubMenuChoiceOutcomes) {
                case 1:
                    // Manual entry
                    CharacterEntry();
                    break;
                case 2:
                    // Batch uploading with Characters_BatchTester.txt,txt
                    CharacterEntryBatch();
                    break;
                case 3:
                    // Exits the submenus
                    System.out.println("Returning to Main Menu");
                    DisplaySubMenu = false;
                    break;
                case 4:
                    System.out.println("\nExiting Program,,, ");
                    DisplaySubMenu = false;
                    this.isRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice, try again (1-4)");
            }
        }
    }


    /**
     * Sub Menu Display for reading entries
     */
    private void SubMenuRead() {
        boolean DisplayReadMenu = true;

        while (DisplayReadMenu) {
            System.out.println("\n=====================================");
            System.out.println("===== ReadAll Or ReadID Options =====");
            System.out.println("=====================================");
            System.out.println("1. Display Entire Character List");
            System.out.println("2. Display by Character ID");
            System.out.println("3. Go Back to Main Menu");
            System.out.println("4. Exit Program");
            System.out.println("\n===================================");

            int SubMenuChoiceOutcomes = UserChoice();

            switch (SubMenuChoiceOutcomes) {
                case 1:
                    //Displays all current entries in Characters.txt.txt
                    DisplayAllCharacters();
                    break;
                case 2:
                    // Displays an entry via ID in Characters.txt.txt
                    DisplayCharacterByID();
                    break;
                case 3:
                    System.out.println("Returning to Main Menu");
                    DisplayReadMenu = false;
                    break;
                case 4:
                    System.out.println("\nExiting Program,,, ");
                    DisplayReadMenu = false;
                    this.isRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice, try again (1-4)");
            }
        }
    }


    /**
     * Sub Menu display for both updating and deleting
     */
    private void SubMenuUpdateDelete() {
        boolean DisplayUpdateDeleteMenu = true;

        while (DisplayUpdateDeleteMenu) {
            System.out.println("\n===================================");
            System.out.println("===== Update / Delete Options =====");
            System.out.println("===================================");
            System.out.println("1. Update a Character via ID");
            System.out.println("2. Delete a Character via ID");
            System.out.println("3. Go Back to Main Menu");
            System.out.println("4. Exit Program");
            System.out.println("\n=================================");

            int SubMenuChoiceOutcomes = UserChoice();

            switch (SubMenuChoiceOutcomes) {
                case 1:
                    // Updates an entry via ID and lets you update each section in the row
                    UpdateEntryID();
                    break;
                case 2:
                    // Removes an entry via ID
                    RemoveEntryID();
                    break;
                case 3:
                    System.out.println("Returning to Main Menu");
                    DisplayUpdateDeleteMenu = false;
                    break;
                case 4:
                    System.out.println("\nExiting Program,,, ");
                    DisplayUpdateDeleteMenu = false;
                    this.isRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice, try again (1-4)");
            }
        }
    }

    /**
     * Users go element by element console prompts for manually making a new entry {@link Characters} and will increment
     * if the ID is already in use. Allows for cancelling in the middle of the entry if you type cancel or cm {@code 'cancel'},
     * {@code 'cm'}
     *
     */
    private void CharacterEntry() {
        System.out.println("\n--- Manual Create Character Entry ---");
        List<Characters> allCharacters = ReadAllFromFile();
        String CharacterIDInput;
        boolean CurrentState;
        String LocationInput;
        boolean ImportanceState;
        String DescriptionInput;

        System.out.println("(Type 'cancel' or 'cm' at any prompt to abort the operation completely)");

        // Makes sure Null ID does not pass during manual entry
        while (true) {
            System.out.print("Enter Character ID (1 or 1.0): ");
            CharacterIDInput = scanner.nextLine().trim();


            if (CharacterIDInput.equalsIgnoreCase("cancel")
                    || CharacterIDInput.equalsIgnoreCase("cm")) {
                System.out.println("Manual character entry operation completely canceled.");
                return;
            }
            if (CharacterIDInput.isEmpty()) {
                System.out.println("[ERROR] ID cannot be empty. Please try again.");
                continue;
            }
            // Allows for avoiding negative numbers and words, only valid Integers even as a String
            if (!CharacterIDInput.matches("^[0-9]+(\\.[0-9]+)?$")) {
                System.out.println("[ERROR] Invalid ID, no words or negative numbers. Please try again.");
                continue;
            }
            break;
        }

        // Processed ID gets displayed in a message
        IDExists = false;
        String verifiedID = IsDuplicate(CharacterIDInput, allCharacters);
        if (!verifiedID.equals(CharacterIDInput)) {
            System.out.println("ID '" + CharacterIDInput +
                    "' already exists. incrementing to next available increment: " + verifiedID);
        }

        //Makes sure only true or false get passed and displayed
        while (true) {
            System.out.print("Enter Current State (True/False or T/F): ");
            String input = scanner.nextLine().trim();


            if (input.equalsIgnoreCase("cancel")
                    || input.equalsIgnoreCase("cm")) {
                System.out.println("Manual character entry operation completely canceled.");
                return;
            }

            if (!input.equalsIgnoreCase("true")
                    && !input.equalsIgnoreCase("false")
                    && !input.equalsIgnoreCase("t")
                    && !input.equalsIgnoreCase("f")) {
                System.out.println("[ERROR] Invalid State. Input must be exactly True, False, T, or F.");
                continue;
            }
            CurrentState = input.equalsIgnoreCase("true")
                    || input.equalsIgnoreCase("t");
            break;
        }

        // Allows for String to be added of any kind, this is closer to flavor text
        while (true) {
            System.out.print("Enter Location (String): ");
            LocationInput = scanner.nextLine().trim();


            if (LocationInput.equalsIgnoreCase("cancel")
                    || LocationInput.equalsIgnoreCase("cm")) {
                System.out.println("Manual character entry operation completely canceled.");
                return;
            }
            if (LocationInput.isEmpty()) {
                System.out.println("[ERROR] Empty Location. Please try again.");
                continue;
            }
            break;
        }

        // True or False displayer
        while (true) {
            System.out.print("Enter ImportanceState (True/False or T/F): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("cancel")
                    || input.equalsIgnoreCase("cm")) {
                System.out.println("Manual character entry operation completely canceled.");
                return;
            }
            if (!input.equalsIgnoreCase("true")
                    && !input.equalsIgnoreCase("false")
                    && !input.equalsIgnoreCase("t")
                    && !input.equalsIgnoreCase("f")) {
                System.out.println("[ERROR] Invalid State. Input must be exactly True, False, T, or F.");
                continue;
            }
            ImportanceState = input.equalsIgnoreCase("true") || input.equalsIgnoreCase("t");
            break;
        }

        /* Displayer for flavor text of this character, name and other important details
         * such if they were important, for what reason and if they are alive in what condition and such.*/
        while (true) {
            System.out.print("Enter Character Description (String): ");
            DescriptionInput = scanner.nextLine().trim();

            if (DescriptionInput.equalsIgnoreCase("cancel")
                    || DescriptionInput.equalsIgnoreCase("cm")) {
                System.out.println("Manual character entry operation canceled.");
                return;
            }
            if (DescriptionInput.isEmpty()) {
                System.out.println("[ERROR] Empty Character Description. Please try again.");
                continue;
            }
            break;
        }

        // Creates the new entry or Incremented entry to then be added into the text file
        boolean success = addCharacter(allCharacters, verifiedID, CurrentState,
                LocationInput, ImportanceState, DescriptionInput);
        if (success) {
            System.out.print("[Success] Created Character Entry into '" + TextFileName + "' file\n");
        }
    }

    /**
     * Prompts for a path/text file name, parses each valid character entry line with {@link #parseBatchLine(String, List)}
     * writes and merges with existing stored area.
     */
    // batch uploading using the Characters_BatchTester.txt file
    private void CharacterEntryBatch() {
        System.out.println("\n--- Batch Upload Character Entry ---");
        System.out.print("Enter path to Text file: ");
        String filePathInput = scanner.nextLine().trim();

        // Strip out any quotation marks
        filePathInput = filePathInput.replace("\"", "");
        String finalPath = filePathInput;

        List<Characters> allCharacters = ReadAllFromFile();
        int databaseStorage = allCharacters.size();
        int importedCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(finalPath))) {
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                boolean lineParsed = parseBatchLine(line, allCharacters);
                if (lineParsed) {
                    importedCount++;
                } else {
                    System.out.println("[ERROR] Line " + lineNumber +
                            ": Contains invalid formatting or missing metrics. Skipping.");
                }
            }

            // Success in batch upload, or if none went through will print zero valid
            if (importedCount > 0) {
                WriteAllToFile(allCharacters);
                System.out.println("[SUCCESS] Successfully processed and ordered " +
                        importedCount + " records.");
            } else {
                System.out.println("[ERROR] Zero valid characters parsed from your file.");
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Cannot read or locate target file path: " + finalPath);
        }
    }

    /**
     * Asks for an existing ID, increments it to next available increment number, updates the changed attributes and
     * commits changes with {@link #updateCharacter(List, String, String, String, String, String)}.
     * */
    private void UpdateEntryID() {
        System.out.println("\n--- Update Existing Character ---");
        System.out.println("Enter base Character ID to update (1 or 1.1) ");
        System.out.print("Type here: ");
        String targetID = scanner.nextLine().trim();


        List<Characters> allCharacters = ReadAllFromFile();
        IDExists = false;

        String nextVersionID = IsDuplicate(targetID, allCharacters);

        // Loops back if an entry entered does not exist
        if (!IDExists) {
            System.out.println("No character entry matching ID '" + targetID + "' was found.");
            return;
        }

        // Successful entry found, allows for updating now. Aborts if wrong info is typed
        System.out.println("\n[Record Found]. Creating incremental update branch version ID | " + nextVersionID);
        System.out.println("\n--- Enter New Configurations details below ---");

        System.out.print("Enter Current State (True or False) ");
        System.out.print("\nType here: ");
        String newState = scanner.nextLine().trim().toLowerCase();
        if (!newState.matches("true|false|t|f")) {
            System.out.println("[ERROR] Invalid State. Update aborted.");
            return;
        }

        System.out.print("\nEnter Location (String) ");
        System.out.print("\nType here: ");
        String newLoc = scanner.nextLine().trim();
        if (newLoc.isEmpty()) {
            System.out.println("[ERROR] Empty Location. Update aborted.");
            return;
        }

        System.out.print("\nEnter ImportanceState (True or False) ");
        System.out.print("\nType here: ");
        String newImp = scanner.nextLine().trim().toLowerCase();
        if (!newState.matches("true|false|t|f")) {
            System.out.println("[ERROR] Invalid State. Update aborted.");
            return;
        }

        System.out.print("\nEnter Character Description (String) ");
        System.out.print("\nType here: ");
        String newDesc = scanner.nextLine().trim();
        if (newDesc.isEmpty()) {
            System.out.println("[ERROR] Empty Character Description. Update aborted.");
            return;
        }

        boolean success = updateCharacter(allCharacters, nextVersionID, newState,
                newLoc, newImp, newDesc);
        if (success) {
            System.out.println("[SUCCESS] Created updated sub-version character entry '"
                    + nextVersionID + "'.");
        }
    }

    /**
     * Targets a specific ID to delete from stored permanently using {@link #deleteCharacter(String, List)}, and we cancel
     * this process with cancel {@code 'cancel'} or cm {@code 'cm'}.
     */
    private void RemoveEntryID() {
        System.out.println("\n--- Delete Character Entry ---");
        System.out.print("Enter Character ID to remove [or type 'cancel'/'cm' to abort]: ");
        System.out.print("Type here: ");
        String targetID = scanner.nextLine().trim();

        if (targetID.equalsIgnoreCase("cancel")
                || targetID.equalsIgnoreCase("cm")
                || targetID.isEmpty()) {
            System.out.println("Deletion operation aborted.");
            return;
        }

        List<Characters> allCharacters = ReadAllFromFile();

        boolean removed = deleteCharacter(targetID, allCharacters);
        if (removed) {
            System.out.println("[SUCCESS] Character ID '" + targetID + "' removed from text file storage.");
        } else {
            System.out.println("No character entry matching ID '" + targetID + "' was found.");
        }
    }

    /**
     * Retrieves all character entries and displays/prints them
     * into the console formated as shown in the print order below as a full list.
     */
    private void DisplayAllCharacters() {
        System.out.println("\n--- Displaying Entire Character Entries in file ---");
        List<Characters> allCharacters = ReadAllFromFile();

        int count = 0;
        for (Characters ch : allCharacters) {
            count++;
            System.out.println("\n-----------------------------------------");
            System.out.println("Character Entry #" + count);
            System.out.println("-----------------------------------------");
            System.out.println("Character ID: " + ch.getCharacterID());
            System.out.println("Current State: " + ch.getCurrentState());
            System.out.println("Location: " + ch.getLocation());
            System.out.println("Importance State: " + ch.getImportanceState());
            System.out.println("Description: " + ch.getCharacterDescription() + "\n");
        }

        if (count == 0) {
            System.out.println(" The text file is currently empty.");
        }
    }

    /**
     * Asks the User for an ID via the console and prints them in the order shown below as a single entry.
     */
    private void DisplayCharacterByID() {
        System.out.println("\n--- Search Character By ID ---");
        System.out.print("Enter Character ID to search: ");
        System.out.print("Type here: ");
        String targetID = scanner.nextLine().trim();

        List<Characters> allCharacters = ReadAllFromFile();
        boolean recordFound = false;

        for (Characters ch : allCharacters) {
            if (ch.getCharacterID().equals(targetID)) {
                System.out.println("\n--- [ID MATCH FOUND] ---");
                System.out.println("Character ID: " + ch.getCharacterID());
                System.out.println("Current State: " + ch.getCurrentState());
                System.out.println("Location: " + ch.getLocation());
                System.out.println("Importance State: " + ch.getImportanceState());
                System.out.println("Description: " + ch.getCharacterDescription());
                recordFound = true;
            }
        }

        if (!recordFound) {
            System.out.println("No character entry matching ID '" + targetID + "' was found.");
        }
    }


    /**
     * Validates and adds {@link Characters}  to the MySQL Database of NarrativeContainerDB
     *
     * @param currentList   The Current list to add to {@link Characters}
     * @param id            Verified ID
     * @param state         Living State
     * @param loc           The Characters location
     * @param imp           Importance State
     * @param desc          Character backstory
     * @return              {@code true} if successfully added, {@code false} if failed to add.
     */
    public boolean addCharacter(List<Characters> currentList, String id, boolean state,
                                String loc, boolean imp, String desc) {
        if (id == null || id.isEmpty() ||
                loc == null || loc.isEmpty() || desc == null || desc.isEmpty()) {
            return false;
        }
        Characters newChar = new Characters(id, state, loc, imp, desc);
        boolean success = currentList.add(newChar);
        if (success && dbController != null) {
            boolean dbSaved = dbController.insertCharacter(newChar);
            if (dbSaved) {
                WriteAllToFile(currentList);
            }
            return dbSaved;
        }
        return success;
    }

    /**
     * Removes the character entry via ID {@code targetID} from the database
     *
     * @param targetID      The ID to remove
     * @param currentList   The current list to remove from {@link Characters}
     * @return              {@code true} takes out matching ID, {@code false} failed to removed
     */
    public boolean deleteCharacter(String targetID, List<Characters> currentList) {
        if (targetID == null || targetID.isEmpty()) return false;
        boolean wasRemoved = currentList.removeIf(ch ->
                ch.getCharacterID().trim().equalsIgnoreCase(targetID.trim()));
        if (wasRemoved && dbController != null) {
            boolean dbDeleted = dbController.deleteCharacterByID(targetID);
            if (dbDeleted) {
                WriteAllToFile(currentList);
            }
            return dbDeleted;
        }
        return wasRemoved;
    }

    /**
     * Commits a newly incremented character ID as a new entry and inserts it into the database
     *
     * @param currentList       Current list being used to update to {@link Characters}
     * @param nextVersionID     The new ID Incremented from the previous version, ({@code "1.2"} --> {@code "1.3"} for example)
     * @param state             Living State
     * @param loc               The Characters location
     * @param imp               Importance State
     * @param desc              Character backstory
     * @return                  {@code true} if added successfully, {@code false} if failed.
     */
    public boolean updateCharacter(List<Characters> currentList, String nextVersionID, String state,
                                   String loc, String imp, String desc) {
        boolean parsedState = state != null && (state.trim().equalsIgnoreCase("true") ||
                state.trim().equalsIgnoreCase("t"));
        boolean parsedImp = imp != null && (imp.trim().equalsIgnoreCase("true") ||
                imp.trim().equalsIgnoreCase("t"));

        Characters updatedChar = new Characters(nextVersionID, Boolean.parseBoolean(state),
                loc, Boolean.parseBoolean(imp), desc);
        boolean success = currentList.add(updatedChar);
        if (success && dbController != null) {
            boolean dbSaved = dbController.insertCharacter(updatedChar);
            if (dbSaved) {
                WriteAllToFile(currentList);
            }
            return dbSaved;
        }
        return success;
    }

    /**
     * The custom method for incrementing ID inputs that exists and increments to the next
     available ID (1 + .1), uses {@code targetID} to see ID number, {@code existingList} to see if it exists as ID already,
     and then uses {@code IDExists} to actually set the new increment.
     * @param targetID          The ID being chosen
     * @param existingList      Current list ID is being checked agaisnt {@link Characters}
     * @return                  If new number is used during the inserted ID {@code targetID} it will be kept like that,
     * if it was existing will be incremented
     */
    public String IsDuplicate(String targetID, List<Characters> existingList) {
        boolean exactMatchFound = false;
        int HighestSubVersionInt = 0;

        String BaseID = targetID.contains(".") ? targetID.split("\\.")[0] : targetID;

        // Check if the EXACT requested ID (like 1.6) is already taken
        for (Characters ch : existingList) {
            if (ch.getCharacterID().trim().equalsIgnoreCase(targetID.trim())) {
                exactMatchFound = true;
                break;
            }
        }


        if (!exactMatchFound) {
            IDExists = false;
            return targetID;
        }

        IDExists = true;
        for (Characters ch : existingList) {
            String currentID = ch.getCharacterID().trim();
            if (currentID.startsWith(BaseID + ".")) {
                try {
                    String subStr = currentID.split("\\.")[1];
                    int subVersionInt = Integer.parseInt(subStr);
                    if (subVersionInt > HighestSubVersionInt) {
                        HighestSubVersionInt = subVersionInt;
                    }
                } catch (Exception ignored) {}
            }
        }

        int nextSubVersion = HighestSubVersionInt + 1;
        return BaseID + "." + nextSubVersion;
    }


    /**
     * Parses with commas separating each attribute from the imported text file, checks for {@link #IsDuplicate(String, List)}
     * duplicate IDs and adds to the new {@link Characters} to {@code currentList}
     *
     * @param rawLine       Formated text line as {@code ID, State, Location, Importance, Description}
     * @param currentList   The list being parsed to {@link Characters}
     * @return              {@code true} if the lines being parsed was successful, {@code false} if soemthing is wrong
     * with a line will not add that line.
     */
    public boolean parseBatchLine(String rawLine, List<Characters> currentList) {
        if (rawLine == null || rawLine.trim().isEmpty()) return false;
        String[] data = rawLine.split(",", -1);
        if (data.length != 5) return false;

        for (String element : data) {
            if (element.trim().isEmpty()) return false;
        }

        try {
            String CharacterID = data[0].trim();

            String CurrentState = data[1].trim().toLowerCase();
            if (!CurrentState.equals("true")
                    && !CurrentState.equals("false")
                    && !CurrentState.equals("t")
                    && !CurrentState.equals("f")) return false;
            boolean currentState = CurrentState.equals("true") || CurrentState.equals("t");

            String Location = data[2].trim();


            String ImportanceState = data[3].trim().toLowerCase();
            if (!ImportanceState.equals("true")
                    && !ImportanceState.equals("false")
                    && !ImportanceState.equals("t")
                    && !ImportanceState.equals("f")) return false;
            boolean importanceState = ImportanceState.equals("true") || ImportanceState.equals("t");

            String Description = data[4].trim();

            String verifiedID = IsDuplicate(CharacterID, currentList);
            return currentList.add(new Characters(verifiedID, currentState, Location, importanceState, Description));
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Entries from the Database {@link DatabaseController} and the text files {@code Characters.txt}
     *
     * @return  Gives a sorted {@link List} of {@link Characters}
     */
    private List<Characters> ReadAllFromFile() {
        if (dbController != null) {
            return dbController.readAllFromDB();
        }
        return ReadAllFromFile(TextFileName);
    }

    /**
     *Reads from specified file inside of project, the characters entries inside being parsed for the project.
     *
     * @param customFilePath    The file being read by the system
     * @return                  {@link List} of {@link Characters} of the parsed file, or can be empty if it could not be read.
     */
    public List<Characters> ReadAllFromFile(String customFilePath) {
        List<Characters> list = new ArrayList<>();
        java.io.File file = new java.io.File(customFilePath);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(customFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",", -1);
                if (data.length >= 5) {
                    list.add(new Characters(
                            data[0].trim(),
                            Boolean.parseBoolean(data[1].trim()),
                            data[2].trim(),
                            Boolean.parseBoolean(data[3].trim()),
                            data[4].trim()
                    ));
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] File reading failed: " + e.getMessage());
        }
        return list;
    }

    /**
     * Sorts the {@link Characters} in nummerical order of 1 and 1.1 in order. Writes to the text file {@code Characters.txt}
     * and jar file version.
     *
     * @param list {@link List} of {@link Characters} get sorted and saved into project.
     */
    private void WriteAllToFile(List<Characters> list) {
        list.sort((c1, c2) -> {
            try {
                // Split IDs by the dot character
                String[] parts1 = c1.getCharacterID().split("\\.");
                String[] parts2 = c2.getCharacterID().split("\\.");

                int base1 = Integer.parseInt(parts1[0]);
                int base2 = Integer.parseInt(parts2[0]);

                if (base1 != base2) {
                    return Integer.compare(base1, base2);
                }

                // If IDs are identical, compares to see which number it will increment to
                int sub1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;
                int sub2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;
                return Integer.compare(sub1, sub2);
            } catch (NumberFormatException e) {
                return c1.getCharacterID().compareTo(c2.getCharacterID());
            }
        });

        // Updates Characters.txt in the project folder
        writeCsvToPath(list, TextFileName);

        // Updates If running from out/artifacts/NarrativeContainer_jar, also syncs the root project Characters.txt
        java.io.File projectRootCopy = new java.io.File("../../../" + TextFileName);
        if (projectRootCopy.exists() && projectRootCopy.isFile()) {
            writeCsvToPath(list, projectRootCopy.getPath());
        }
    }

    /**
     * Organizes a list of {@link Characters} into a comma separated format and overwrites the current file with the new changes
     *
     * @param list          The sortes {@link List} of {@link Characters} to write
     * @param targetPath    The file to update with the changes
     */
    private void writeCsvToPath(List<Characters> list, String targetPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetPath, false))) {
            for (Characters ch : list) {
                String csvRow = String.format("%s, %b, %s, %b, %s",
                        ch.getCharacterID(),
                        ch.getCurrentState(),
                        ch.getLocation(),
                        ch.getImportanceState(),
                        ch.getCharacterDescription()
                );
                bw.write(csvRow);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] File writing failed for " + targetPath + ": " + e.getMessage());
        }
    }


    /**
     * Starts the program loop when called to authenticate the connection and credentials to the MySQL database and then
     * executing the CRUD operations system of the application.
     */
    public void start() {
        // Database connection added to the starting
        initializeDatabaseConnection();
        while (isRunning) {
            DisplayMenu();
            int choice = UserChoice();
            MenuChoiceOutcomes(choice);
        }
    }
}
