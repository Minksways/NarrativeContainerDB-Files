import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates and handles the JDBC connection, uses SQL database operations against MySQL {@code NarrativeContainerDB} database.
 * <p>
 * Handles login information to get into the database access via JDBC strings supplied by the user supplied credentials,
 * establishes connection, executes SQL operations made to query, sort, insert, and delete {@link Characters}
 * </p>
 *
 * @author Michael (Minks) Cortes Muniz
 */
public class DatabaseController {
    /** JDBC Connection URL to target the MySQL host and {@code NarrativeContainerDB} schema*/
        private String url;
    /** Username supplied at runtime*/
        private String user;
    /** Password supplied at runtime*/
        private String password;

    /**
     * Constructs new {@code DatabaseController} with user supplied credentials
     *
     * @param hostAddress   The MySQL server hostname {@code "localhost3306"}
     * @param user          The MySQL username used to authenticate
     * @param password      The MySQL password used to authenticate
     */
        public DatabaseController(String hostAddress, String user, String password) {
            // Formats address like "localhost:3306" or "127.0.0.1" into a full JDBC URL
            if (!hostAddress.contains(":")) {
                hostAddress = hostAddress + ":3306";
            }
            this.url = "jdbc:mysql://" + hostAddress + "/NarrativeContainerDB?useSSL=false&allowPublicKeyRetrieval=true";
            this.user = user;
            this.password = password;
        }

    /**
     * Tests whether a valid connection was established with the inputs
     *
     * @return  {@code true} if connection was established, {@code false} if not established and loops the DatabaseController
     */
    public boolean testConnection() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("[DB ERROR] Could not connect: " + e.getMessage());
            return false;
        }
    }


    /**
     * Opens a connection {@link Connection} to the {@code NarrativeContainerDB}
     *
     * @return                  a JDBC {@link Connection} object
     * @throws SQLException     If access errors occur or if credentials do not match.
     */
    public Connection connect() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }


    /**
     * Queries all the rows from {@code characters} in MySQL tables, and puts them to {@link Characters} object, sorting everything/
     *
     * @return  Sorted {@llink List} of {@link Characters}
     */
        public List<Characters> readAllFromDB() {
            List<Characters> list = new ArrayList<>();
            String query = "SELECT * FROM characters";

            try (Connection conn = connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    list.add(new Characters(
                            rs.getString("character_id"),
                            rs.getBoolean("current_state"),
                            rs.getString("location"),
                            rs.getBoolean("importance_state"),
                            rs.getString("character_description")
                    ));
                }
            } catch (SQLException e) {
                System.out.println("[DB ERROR] Failed to read characters: " + e.getMessage());
            }

            // Keeps your custom double-index sorting intact
            list.sort((c1, c2) -> {
                try {
                    String[] parts1 = c1.getCharacterID().split("\\.");
                    String[] parts2 = c2.getCharacterID().split("\\.");
                    int base1 = Integer.parseInt(parts1[0]);
                    int base2 = Integer.parseInt(parts2[0]);
                    if (base1 != base2) return Integer.compare(base1, base2);
                    int sub1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;
                    int sub2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;
                    return Integer.compare(sub1, sub2);
                } catch (NumberFormatException e) {
                    return c1.getCharacterID().compareTo(c2.getCharacterID());
                }
            });
            return list;
        }

    /**
     * Inserts into {@link Characters} into {@code characters} table using {@link PreparedStatement} to avoid SQL Injections
     * @param ch    The {@link Characters} with the attributes to insert into MySQL
     * @return      {@code true} if at least one row was a success, {@code false} if insert failed
     */
        public boolean insertCharacter(Characters ch) {
            String sql = "INSERT INTO characters (character_id, current_state, " +
                    "location, importance_state, character_description) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ch.getCharacterID());
                ps.setBoolean(2, ch.getCurrentState());
                ps.setString(3, ch.getLocation());
                ps.setBoolean(4, ch.getImportanceState());
                ps.setString(5, ch.getCharacterDescription());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println("[DB ERROR] Insert failed: " + e.getMessage());
                return false;
            }
        }

    /**
     * Deletes a row from {@code characters} table if ID matches and is confirmed.
     *
     * @param targetID  Unique ID
     * @return          {@code true} if removed, {@code false} if failed
     */
        public boolean deleteCharacterByID(String targetID) {
            String sql = "DELETE FROM characters WHERE character_id = ?";
            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, targetID.trim());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println("[DB ERROR] Delete failed: " + e.getMessage());
                return false;
            }
        }
    }
