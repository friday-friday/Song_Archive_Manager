import java.sql.*;
import java.util.*;

// ─────────────────────────────────────────────
//  DATABASE CONFIGURATION
// ─────────────────────────────────────────────
class DBConfig {
    public static final String DB_NAME   = "song_archive";
    public static final String URL       = "jdbc:mysql://localhost:3306/" + DB_NAME;
    public static final String BASE_URL  = "jdbc:mysql://localhost:3306/";
    public static final String USER      = "root";
    public static final String PASSWORD  = "8008";
}

// ─────────────────────────────────────────────
//  MODEL
// ─────────────────────────────────────────────
class Song {
    private int    id;
    private String title;
    private String artist;
    private String genre;
    private int    releaseYear;

    public Song() {}

    public Song(int id, String title, String artist, String genre, int releaseYear) {
        this.id          = id;
        this.title       = title;
        this.artist      = artist;
        this.genre       = genre;
        this.releaseYear = releaseYear;
    }

    public Song(String title, String artist, String genre, int releaseYear) {
        this(0, title, artist, genre, releaseYear);
    }

    public int    getId()          { return id; }
    public void   setId(int id)    { this.id = id; }
    public String getTitle()       { return title; }
    public void   setTitle(String title) { this.title = title; }
    public String getArtist()      { return artist; }
    public void   setArtist(String artist) { this.artist = artist; }
    public String getGenre()       { return genre; }
    public void   setGenre(String genre) { this.genre = genre; }
    public int    getReleaseYear() { return releaseYear; }
    public void   setReleaseYear(int y) { this.releaseYear = y; }

    @Override
    public String toString() {
        return String.format("%-4d | %-30s | %-25s | %-15s | %-4d",
                id, title, artist, genre, releaseYear);
    }
}

// ─────────────────────────────────────────────
//  DATABASE CONNECTION & INITIALISATION
// ─────────────────────────────────────────────
class DBConnection {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(
                DBConfig.BASE_URL, DBConfig.USER, DBConfig.PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConfig.DB_NAME);
            stmt.executeUpdate("USE " + DBConfig.DB_NAME);

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS songs (" +
                "  id           INT AUTO_INCREMENT PRIMARY KEY," +
                "  title        VARCHAR(255) NOT NULL," +
                "  artist       VARCHAR(255) NOT NULL," +
                "  genre        VARCHAR(100)," +
                "  release_year INT" +
                ")"
            );
            System.out.println("  Database ready: '" + DBConfig.DB_NAME + "' / table 'songs'.");

        } catch (SQLException e) {
            System.err.println("  [DB INIT ERROR] " + e.getMessage());
        }
    }
}

// ─────────────────────────────────────────────
//  DATA ACCESS OBJECT  (CRUD + KEYWORD SEARCH)
// ─────────────────────────────────────────────
class SongDAO {

    // ── CREATE ──────────────────────────────────
    public void addSong(Song song) {
        String sql = "INSERT INTO songs (title, artist, genre, release_year) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getGenre());
            ps.setInt(4, song.getReleaseYear());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) song.setId(keys.getInt(1));
            }
            System.out.println("  Song added (ID: " + song.getId() + ").");

        } catch (SQLException e) { System.err.println("  [ADD ERROR] " + e.getMessage()); }
    }

    // ── READ ALL ────────────────────────────────
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery("SELECT * FROM songs ORDER BY id")) {

            while (rs.next()) songs.add(mapRow(rs));

        } catch (SQLException e) { System.err.println("  [FETCH ERROR] " + e.getMessage()); }
        return songs;
    }

    // ── READ BY ID ──────────────────────────────
    public Song getSongById(int id) {
        String sql = "SELECT * FROM songs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("  [SEARCH ERROR] " + e.getMessage()); }
        return null;
    }

    // ── FEATURE: SEARCH BY TITLE OR ARTIST ──
    public List<Song> searchByTitleOrArtist(String keyword) {
        List<Song> results = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE title LIKE ? OR artist LIKE ? ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("  [SEARCH ERROR] " + e.getMessage()); }
        return results;
    }

    // ── UPDATE ──────────────────────────────────
    public void updateSong(Song song) {
        String sql = "UPDATE songs SET title=?, artist=?, genre=?, release_year=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, song.getTitle());
            ps.setString(2, song.getArtist());
            ps.setString(3, song.getGenre());
            ps.setInt(4, song.getReleaseYear());
            ps.setInt(5, song.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "  Song updated." : "  No record updated.");

        } catch (SQLException e) { System.err.println("  [UPDATE ERROR] " + e.getMessage()); }
    }

    // ── DELETE ──────────────────────────────────
    public void deleteSong(int id) {
        String sql = "DELETE FROM songs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            System.out.println(ps.executeUpdate() > 0 ? "  Song deleted." : "  Song not found.");

        } catch (SQLException e) { System.err.println("  [DELETE ERROR] " + e.getMessage()); }
    }

    // ── HELPER ──────────────────────────────────
    private Song mapRow(ResultSet rs) throws SQLException {
        return new Song(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("artist"),
            rs.getString("genre"),
            rs.getInt("release_year")
        );
    }
}

// ─────────────────────────────────────────────
//  MAIN APPLICATION
// ─────────────────────────────────────────────
public class SongArchive {

    // ── CONSTANTS ───────────────────────────────
    private static final Scanner scanner = new Scanner(System.in);
    private static final SongDAO songDAO = new SongDAO();

    private static final String RESET      = "\u001B[0m";
    private static final String BOLD       = "\u001B[1m";
    private static final String PURPLE     = "\u001B[35m";
    private static final String RED_BG     = "\u001B[41m";
    private static final String BLUE_BG    = "\u001B[44m";
    private static final String CYAN_BG    = "\u001B[46m";
    private static final String BLACK_TEXT = "\u001B[30m";
    private static final String WHITE_TEXT = "\u001B[37m";
    private static final String YELLOW     = "\u001B[33m";
    private static final String CYAN       = "\u001B[36m";
    private static final String GREEN      = "\u001B[32m";

    private static final String DIVIDER = "  " + "-".repeat(87);
    private static final String HEADER_FMT = "  %-4s | %-30s | %-25s | %-15s | %-4s%n";

    // ── ENTRY POINT ─────────────────────────────
    public static void main(String[] args) {
        showWelcome();
        DBConnection.initializeDatabase();

        while (true) {
            System.out.println("\n" + PURPLE + "»".repeat(60) + RESET);
            showMenu();

            int choice = readInt("  ➤ YOUR CHOICE: ");
            if (choice == -1) continue;

            switch (choice) {
                case 1 -> addSong();
                case 2 -> viewSongs();
                case 3 -> searchByKeyword();      
                case 4 -> searchById();
                case 5 -> updateSong();
                case 6 -> deleteSong();
                case 7 -> shutdown();
                default -> System.out.println(RED_BG + WHITE_TEXT + " [!] Enter 1-7 only. " + RESET);
            }
        }
    }

    // ── MENU ────────────────────────────────────
    private static void showMenu() {
        String line = "  " + "=".repeat(50) + "  ";
        System.out.println(CYAN_BG + BLACK_TEXT + BOLD + "               S O N G   A R C H I V E                " + RESET);
        System.out.println(BLUE_BG + WHITE_TEXT + line + RESET);
        printOption("1", "+  ADD TRACK");
        printOption("2", "\uD83D\uDCDC  BROWSE ALL RECORDS");
        printOption("3", "\uD83D\uDD0E  SEARCH BY TITLE / ARTIST");  
        printOption("4", "\uD83D\uDD22  SEARCH BY ID");
        printOption("5", "\uD83D\uDCDD  EDIT METADATA");
        printOption("6", "\u274C  DELETE PERMANENTLY");
        System.out.println(BLUE_BG + WHITE_TEXT + line + RESET);
        printOption("7", "\uD83D\uDEAA  SHUTDOWN");
        System.out.println(BLUE_BG + WHITE_TEXT + line + RESET);
    }

    private static void printOption(String num, String label) {
        int displayWidth = 0;
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c <= 127) {
                displayWidth++;
            } else {
                displayWidth += 2;
                if (Character.isHighSurrogate(c)) {
                    i++;
                }
            }
        }

        System.out.println(BLUE_BG + WHITE_TEXT + "  \u2551 " + BOLD
                + " [" + num + "]  " + label
                + " ".repeat(Math.max(0, 40 - displayWidth))
                + WHITE_TEXT + " \u2551  " + RESET);
    }

    // ── WELCOME SCREEN ──────────────────────────
    private static void showWelcome() {
        String banner = CYAN + BOLD
            + "  *****************************************************\n"
            + "  * *\n"
            + "  * SONG ARCHIVE MANAGER  v3.0                    *\n"
            + "  * CRUD Application  |  Java + MySQL             *\n"
            + "  * *\n"
            + "  *****************************************************" + RESET;

        for (String line : banner.split("\n")) {
            System.out.println(line);
            sleep(45);
        }
        System.out.println("\n" + BLUE_BG + WHITE_TEXT + BOLD
                + "   >>> DATABASE CONNECTED  |  READY   <<<   " + RESET);
    }

    // ── LOADING SPINNER ─────────────────────────
    private static void showLoading(String msg) {
        System.out.print("\n  " + BOLD + YELLOW + msg + " " + RESET);
        char[] spin = {'|', '/', '-', '\\'};
        for (int i = 0; i < 8; i++) {
            System.out.print(CYAN + spin[i % 4] + RESET);
            sleep(80);
            System.out.print("\b");
        }
        System.out.println(GREEN + "DONE" + RESET);
    }

    // ── UTILITY: SLEEP ──────────────────────────
    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ── UTILITY: READ INTEGER ───────────────────
    private static int readInt(String prompt) {
        System.out.print(BOLD + CYAN + prompt + RESET);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(RED_BG + WHITE_TEXT + " [!] Numbers only! " + RESET);
            return -1;
        }
    }

    // ── UTILITY: READ NON-EMPTY STRING ──────────
    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // ── UTILITY: PRINT TABLE ────────────────────
    private static void printTable(List<Song> songs) {
        System.out.println(CYAN + DIVIDER + RESET);
        System.out.printf(BOLD + HEADER_FMT + RESET, "ID", "TITLE", "ARTIST", "GENRE", "YEAR");
        System.out.println(CYAN + DIVIDER + RESET);
        for (Song s : songs) System.out.println("  " + s);
        System.out.println(CYAN + DIVIDER + RESET);
        System.out.println("  " + songs.size() + " record(s) found.");
    }

    // ── FEATURE 1: ADD SONG ─────────────────────
    private static void addSong() {
        System.out.println("\n" + BLUE_BG + WHITE_TEXT + BOLD + "  --- REGISTER NEW SONG ---  " + RESET);
        String title  = readString("  Title  : ");
        String artist = readString("  Artist : ");
        String genre  = readString("  Genre  : ");
        int    year   = readInt("  Year   : ");
        if (year == -1) return;

        if (title.isEmpty() || artist.isEmpty()) {
            System.out.println(RED_BG + WHITE_TEXT + " [!] Title and Artist are required! " + RESET);
            return;
        }

        showLoading("Saving to archive... ");
        songDAO.addSong(new Song(title, artist, genre, year));
    }

    // ── FEATURE 2: VIEW ALL ─────────────────────
    private static void viewSongs() {
        System.out.println("\n" + BLUE_BG + WHITE_TEXT + BOLD + "  --- ARCHIVE INVENTORY ---  " + RESET + "\n");
        List<Song> songs = songDAO.getAllSongs();
        if (songs.isEmpty())
            System.out.println("  [!] Archive is empty.");
        else
            printTable(songs);
    }

    // ── FEATURE 3 : SEARCH BY TITLE/ARTIST ─
    private static void searchByKeyword() {
        String keyword = readString("\n  Enter title / artist keyword: ");
        if (keyword.isEmpty()) {
            System.out.println(RED_BG + WHITE_TEXT + " [!] Keyword cannot be blank. " + RESET);
            return;
        }
        showLoading("Searching archive... ");
        List<Song> results = songDAO.searchByTitleOrArtist(keyword);
        System.out.println("\n" + BOLD + YELLOW + "  Results for: \"" + keyword + "\"" + RESET);
        if (results.isEmpty())
            System.out.println("  [!] No matches found.");
        else
            printTable(results);
    }

    // ── FEATURE 4: SEARCH BY ID ─────────────────
    private static void searchById() {
        int id = readInt("\n  Enter song ID: ");
        if (id == -1) return;
        showLoading("Querying database... ");
        Song song = songDAO.getSongById(id);
        if (song != null) {
            System.out.println();
            printTable(Collections.singletonList(song));
        } else {
            System.out.println("  [!] Track #" + id + " not found.");
        }
    }

    // ── FEATURE 5: UPDATE SONG ──────────────────
    private static void updateSong() {
        int id = readInt("\n  Enter ID to update: ");
        if (id == -1) return;

        Song song = songDAO.getSongById(id);
        if (song == null) { System.out.println("  [!] Record not found."); return; }

        System.out.println("\n" + YELLOW + BOLD + "  Updating: \"" + song.getTitle() + "\"" + RESET);
        System.out.print("  New Title  [" + song.getTitle()  + "]: "); String t = scanner.nextLine().trim();
        System.out.print("  New Artist [" + song.getArtist() + "]: "); String a = scanner.nextLine().trim();
        System.out.print("  New Genre  [" + song.getGenre()  + "]: "); String g = scanner.nextLine().trim();
        System.out.print("  New Year   [" + song.getReleaseYear() + "]: "); String y = scanner.nextLine().trim();

        if (!t.isEmpty()) song.setTitle(t);
        if (!a.isEmpty()) song.setArtist(a);
        if (!g.isEmpty()) song.setGenre(g);
        if (!y.isEmpty()) {
            try   { song.setReleaseYear(Integer.parseInt(y)); }
            catch (NumberFormatException e) { System.out.println("  [!] Invalid year — keeping original."); }
        }

        showLoading("Committing changes... ");
        songDAO.updateSong(song);
    }

    // ── FEATURE 6: DELETE SONG ──────────────────
    private static void deleteSong() {
        int id = readInt("\n  Enter ID to delete: ");
        if (id == -1) return;

        Song song = songDAO.getSongById(id);
        if (song == null) { System.out.println("  [!] Song #" + id + " not found."); return; }

        System.out.print("  Confirm delete \"" + song.getTitle() + "\" ? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("  Delete cancelled.");
            return;
        }

        showLoading("Deleting permanently... ");
        songDAO.deleteSong(id);
    }

    // ── FEATURE 7: SHUTDOWN ─────────────────────
    private static void shutdown() {
        System.out.println(PURPLE + BOLD + "\n  Goodbye! Keep rocking!\n" + RESET);
        scanner.close();
        System.exit(0);
    }
}