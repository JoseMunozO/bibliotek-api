package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.BookDAO;
import se.josecarlos.bibliotek.dto.BookDTO;
import se.josecarlos.bibliotek.dto.BookDetailsDTO;
import se.josecarlos.bibliotek.dto.BookStatisticsDTO;

import java.util.Comparator;
import java.util.List;

public class BookService {

    private final BookDAO bookDAO;

    public BookService() {
        this.bookDAO = new BookDAO();
    }

    public List<BookDTO> getAllBooks() {
        return bookDAO.getCatalogBooks().stream()
                .sorted(Comparator.comparing(BookDTO::getTitle))
                .toList();
    }

    public List<BookDTO> getAvailableBooks() {
        return bookDAO.getCatalogBooks().stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .sorted(Comparator.comparing(BookDTO::getTitle))
                .toList();
    }

    public List<BookDTO> searchBooks(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isEmpty()) {
            return List.of();
        }

        return bookDAO.getCatalogBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(normalizedKeyword.toLowerCase()))
                .sorted(Comparator.comparing(BookDTO::getTitle))
                .toList();
    }

    public List<BookDTO> getBooksSortedBy(String sortBy) {
        Comparator<BookDTO> comparator = switch (sortBy == null ? "" : sortBy.trim().toLowerCase()) {
            case "id" -> Comparator.comparingInt(BookDTO::getId);
            case "author" -> Comparator.comparing((BookDTO book) -> normalizeSortValue(book.getAuthors()))
                    .thenComparing(BookDTO::getTitle);
            case "name", "title" -> Comparator.comparing((BookDTO book) -> normalizeSortValue(book.getTitle()));
            default -> Comparator.comparing((BookDTO book) -> normalizeSortValue(book.getTitle()));
        };

        return bookDAO.getCatalogBooks().stream()
                .sorted(comparator)
                .toList();
    }

    public BookDetailsDTO getBookDetails(int id) {
        if (id <= 0) {
            throw new ValidationException("Invalid book ID.");
        }

        BookDetailsDTO details = bookDAO.getBookDetails(id);
        if (details == null) {
            throw new NotFoundException("Book not found.");
        }

        return details;
    }

    public List<BookStatisticsDTO> getMostBorrowedBooks(int limit) {
        int normalizedLimit = limit <= 0 ? 10 : limit;
        return bookDAO.getMostBorrowedBooks(normalizedLimit);
    }

    private String normalizeSortValue(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
