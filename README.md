# Song Archive Manager v3.0

A professional CLI-based CRUD application built with Java and MySQL for managing a music archive. This tool allows users to register songs, browse records, search by keyword, and maintain a persistent database of their favorite tracks.

## 🚀 Features

- **Full CRUD Support**: Create, Read, Update, and Delete song records.
- **Smart Search**: Find tracks by Title, Artist, or specific ID.
- **Automated Database Setup**: Automatically creates the `song_archive` database and `songs` table on first run.
- **Rich CLI Experience**: Interactive menu with ANSI colors, loading spinners, and formatted tables.
- **Input Validation**: Ensures data integrity with robust input handling.

## 🛠️ Tech Stack

- **Language**: Java
- **Database**: MySQL
- **Connectivity**: JDBC (MySQL Connector/J)

## 📋 Prerequisites

- **Java Development Kit (JDK)**: 17 or higher recommended.
- **MySQL Server**: Running on `localhost:3306`.
- **MySQL Connector/J**: `mysql-connector-j-9.7.0.jar` (included in the repository).

## ⚙️ Configuration

Before running the application, ensure your MySQL credentials match the configuration in `SongArchive.java`:

```java
class DBConfig {
    public static final String USER      = "root";
    public static final String PASSWORD  = "8008"; // Update this to your MySQL password
}
```

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/song-archive-manager.git
cd song-archive-manager
```

### 2. Compile the application
```bash
javac -cp ".:mysql-connector-j-9.7.0.jar" SongArchive.java
```
*Note: Use `;` instead of `:` as a separator on Windows.*

### 3. Run the application
```bash
java -cp ".:mysql-connector-j-9.7.0.jar" SongArchive
```

## 📖 Usage

Upon launching, the application will initialize the database. Follow the on-screen menu to:
1. **Add Track**: Register a new song with title, artist, genre, and year.
2. **Browse All Records**: View a formatted table of all saved songs.
3. **Search by Title/Artist**: Filter records using keywords.
4. **Search by ID**: Find a specific track by its unique ID.
5. **Edit Metadata**: Update existing song details.
6. **Delete Permanently**: Remove a record from the archive.
7. **Shutdown**: Safely exit the application.

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
