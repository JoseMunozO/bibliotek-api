package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.BookDetailsDTO;
import se.josecarlos.bibliotek.dto.BookDTO;
import se.josecarlos.bibliotek.dto.BookStatisticsDTO;
import se.josecarlos.bibliotek.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<BookDTO> getCatalogBooks() {
        List<BookDTO> books = new ArrayList<>();
        String sql = """
                SELECT
                    b.id,
                    b.title,
                    b.available_copies,
                    COALESCE(GROUP_CONCAT(DISTINCT CONCAT(a.first_name, ' ', a.last_name)
                        ORDER BY a.last_name, a.first_name SEPARATOR ', '), '') AS authors
                FROM books b
                LEFT JOIN book_authors ba ON ba.book_id = b.id
                LEFT JOIN authors a ON a.id = ba.author_id
                GROUP BY b.id, b.title, b.available_copies
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(new BookDTO(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getInt("available_copies"),
                        rs.getString("authors")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch book " + id, e);
        }

        return null;
    }

    public List<Book> searchBooksByTitle(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return books;
    }

    public BookDetailsDTO getBookDetails(int id) {
        String sql = """
                SELECT
                    b.id,
                    b.title,
                    b.isbn,
                    b.year_published,
                    b.total_copies,
                    b.available_copies,
                    bd.summary,
                    bd.language,
                    bd.page_count,
                    COALESCE(GROUP_CONCAT(DISTINCT CONCAT(a.first_name, ' ', a.last_name)
                        ORDER BY a.last_name, a.first_name SEPARATOR ', '), '') AS authors,
                    COALESCE(GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ', '), '') AS categories
                FROM books b
                LEFT JOIN book_descriptions bd ON bd.book_id = b.id
                LEFT JOIN book_authors ba ON ba.book_id = b.id
                LEFT JOIN authors a ON a.id = ba.author_id
                LEFT JOIN book_categories bc ON bc.book_id = b.id
                LEFT JOIN categories c ON c.id = bc.category_id
                WHERE b.id = ?
                GROUP BY b.id, b.title, b.isbn, b.year_published, b.total_copies, b.available_copies,
                         bd.summary, bd.language, bd.page_count
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer pageCount = (Integer) rs.getObject("page_count");

                    return new BookDetailsDTO(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("isbn"),
                            rs.getInt("year_published"),
                            rs.getInt("total_copies"),
                            rs.getInt("available_copies"),
                            rs.getString("summary"),
                            rs.getString("language"),
                            pageCount,
                            rs.getString("authors"),
                            rs.getString("categories")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<BookStatisticsDTO> getMostBorrowedBooks(int limit) {
        List<BookStatisticsDTO> statistics = new ArrayList<>();
        String sql = """
                SELECT
                    b.id,
                    b.title,
                    COUNT(l.id) AS loan_count
                FROM books b
                LEFT JOIN loans l ON l.book_id = b.id
                GROUP BY b.id, b.title
                ORDER BY loan_count DESC, b.title ASC
                LIMIT ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statistics.add(new BookStatisticsDTO(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getLong("loan_count")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statistics;
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("isbn"),
                rs.getInt("year_published"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }

    public void decreaseAvailableCopies(int bookId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            decreaseAvailableCopies(conn, bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean decreaseAvailableCopies(Connection conn, int bookId) {
        String sql = """
                UPDATE books
                SET available_copies = available_copies - 1
                WHERE id = ? AND available_copies > 0
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not decrease available copies for book " + bookId, e);
        }
    }

    public void increaseAvailableCopies(int bookId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            increaseAvailableCopies(conn, bookId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean increaseAvailableCopies(Connection conn, int bookId) {
        String sql = """
                UPDATE books
                SET available_copies = available_copies + 1
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not increase available copies for book " + bookId, e);
        }
    }
}
