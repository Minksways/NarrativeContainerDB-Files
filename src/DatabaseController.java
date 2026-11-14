import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
        private String url;
        private String user;
        private String password;

        public DatabaseController(String hostAddress, String user, String password) {
            // Formats address like "localhost:3306" or "127.0.0.1" into a full JDBC URL
            if (!hostAddress.contains(":")) {
                hostAddress = hostAddress + ":3306";
            }
            this.url = "jdbc:mysql://" + hostAddress + "/NarrativeContainerDB?useSSL=false&allowPublicKeyRetrieval=true";
            this.user = user;
            this.password = password;
        }
    public boolean testConnection() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("[DB ERROR] Could not connect: " + e.getMessage());
            return false;
        }
    }


    public Connection connect() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }

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
