# Song Archive Manager

A console-based Java application that connects to a MySQL database via JDBC to manage a music collection. It allows users to easily Create, Read, Update, Delete, and Search song records through a fully interactive terminal menu, preventing data loss and duplication compared to manual spreadsheets.

## 🚀 Features

1. **Add Track (Create)**: Prompts for title, artist, genre, and year, then inserts a new row with an auto-increment ID.
2. **Browse Records (Read)**: Retrieves and displays all songs as a formatted table ordered by ID.
3. **Search by Keyword**: Partial-string search by title or artist using SQL `LIKE` — no ID required. Safely parameterized to prevent SQL injection.
4. **Search by ID**: Fetches a single song by its primary key.
5. **Edit Metadata (Update)**: Loads an existing song, shows current values, accepts optional field updates, and saves changes.
6. **Delete Permanently**: Hard-deletes a record by ID after an explicit yes/no confirmation prompt.

## 🛠 Tech Stack

| Component | Detail |
| :--- | :--- |
| **Programming Language** | Java SE (JDK 17+) |
| **Database Engine** | MySQL 8.x Server |
| **Connection Bridge** | JDBC (Java Database Connectivity) |
| **Database Driver** | `mysql-connector-j-9.7.0.jar` |
| **Architecture** | Single-file, multi-class console app |
| **Design Pattern** | Data Access Object (DAO) |

## 🗄️ Database Schema

The application automatically creates the `song_archive` database and the `songs` table on its first run if they do not exist.

```sql
CREATE DATABASE IF NOT EXISTS song_archive;
USE song_archive;

CREATE TABLE IF NOT EXISTS songs (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(255) NOT NULL,
  artist       VARCHAR(255) NOT NULL,
  genre        VARCHAR(100),
  release_year INT
);
