package se.josecarlos.bibliotek.presentation;

import se.josecarlos.bibliotek.business.BookService;
import se.josecarlos.bibliotek.business.ReviewService;
import se.josecarlos.bibliotek.dto.BookDTO;
import se.josecarlos.bibliotek.dto.BookDetailsDTO;
import se.josecarlos.bibliotek.dto.BookStatisticsDTO;
import se.josecarlos.bibliotek.dto.ReviewDTO;

import java.util.List;
import java.util.Scanner;

public class BookMenu {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final Scanner scanner;

    public BookMenu() {
        this.bookService = new BookService();
        this.reviewService = new ReviewService();
        this.scanner = new Scanner(System.in);
    }

    public void showBookMenu() {
        int choice;

        do {
            System.out.println("\n--- BOOK MENU ---");
            System.out.println("1. Show all books");
            System.out.println("2. Sort books");
            System.out.println("3. Show available books");
            System.out.println("4. Search books by title");
            System.out.println("5. Show book details");
            System.out.println("6. Show top borrowed books");
            System.out.println("7. Show book reviews");
            System.out.println("8. Add review");
            System.out.println("0. Back / Exit");
            choice = MenuInput.readInt(scanner, "Choose an option: ");

            switch (choice) {
                case 1 -> showAllBooks();
                case 2 -> sortBooks();
                case 3 -> showAvailableBooks();
                case 4 -> searchBooks();
                case 5 -> showBookDetails();
                case 6 -> showTopBorrowedBooks();
                case 7 -> showBookReviews();
                case 8 -> addReview();
                case 0 -> System.out.println("Leaving Book Menu...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    private void showAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();

        System.out.println("\n--- ALL BOOKS ---");
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        for (BookDTO book : books) {
            System.out.println("ID: " + book.getId()
                    + " | Title: " + book.getTitle()
                    + " | Authors: " + safeValue(book.getAuthors())
                    + " | Available copies: " + book.getAvailableCopies());
        }
    }

    private void showAvailableBooks() {
        List<BookDTO> books = bookService.getAvailableBooks();

        System.out.println("\n--- AVAILABLE BOOKS ---");
        if (books.isEmpty()) {
            System.out.println("No available books.");
            return;
        }
        for (BookDTO book : books) {
            System.out.println("ID: " + book.getId()
                    + " | Title: " + book.getTitle()
                    + " | Authors: " + safeValue(book.getAuthors())
                    + " | Available copies: " + book.getAvailableCopies());
        }
    }

    private void searchBooks() {
        String keyword = MenuInput.readRequiredString(scanner, "Enter title keyword: ");

        List<BookDTO> books = bookService.searchBooks(keyword);

        System.out.println("\n--- SEARCH RESULTS ---");
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        for (BookDTO book : books) {
            System.out.println("ID: " + book.getId()
                    + " | Title: " + book.getTitle()
                    + " | Authors: " + safeValue(book.getAuthors())
                    + " | Available copies: " + book.getAvailableCopies());
        }
    }

    private void showBookDetails() {
        int id = MenuInput.readPositiveInt(scanner, "Enter book id: ");

        BookDetailsDTO book = bookService.getBookDetails(id);

        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        System.out.println("\n--- BOOK DETAILS ---");
        System.out.println("ID: " + book.getId());
        System.out.println("Title: " + book.getTitle());
        System.out.println("ISBN: " + book.getIsbn());
        System.out.println("Year published: " + book.getYearPublished());
        System.out.println("Total copies: " + book.getTotalCopies());
        System.out.println("Available copies: " + book.getAvailableCopies());
        System.out.println("Language: " + safeValue(book.getLanguage()));
        System.out.println("Page count: " + (book.getPageCount() != null ? book.getPageCount() : "N/A"));
        System.out.println("Authors: " + safeValue(book.getAuthors()));
        System.out.println("Categories: " + safeValue(book.getCategories()));
        System.out.println("Summary: " + safeValue(book.getSummary()));
    }

    private String safeValue(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }

        return value;
    }

    private void showTopBorrowedBooks() {
        int limit = MenuInput.readPositiveInt(scanner, "How many books to show: ");
        List<BookStatisticsDTO> statistics = bookService.getMostBorrowedBooks(limit);

        if (statistics.isEmpty()) {
            System.out.println("No statistics found.");
            return;
        }

        System.out.println("\n--- MOST BORROWED BOOKS ---");
        for (BookStatisticsDTO stat : statistics) {
            System.out.println(
                    "Book ID: " + stat.getBookId()
                            + " | Title: " + stat.getTitle()
                            + " | Total loans: " + stat.getLoanCount()
            );
        }
    }

    private void showBookReviews() {
        int bookId = MenuInput.readPositiveInt(scanner, "Book ID: ");
        List<ReviewDTO> reviews;

        try {
            reviews = reviewService.getReviewsByBookId(bookId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (reviews.isEmpty()) {
            System.out.println("No reviews found for this book.");
            return;
        }

        System.out.println("\n--- BOOK REVIEWS ---");
        for (ReviewDTO review : reviews) {
            System.out.println(
                    "Review ID: " + review.getId()
                            + " | Member: " + review.getMemberName()
                            + " | Rating: " + review.getRating()
                            + " | Date: " + review.getReviewDate()
            );

            String comment = review.getComment();
            if (comment != null && !comment.isBlank()) {
                System.out.println("Comment: " + comment);
            }
        }
    }

    private void addReview() {
        int bookId = MenuInput.readPositiveInt(scanner, "Book ID: ");
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        int rating = MenuInput.readInt(scanner, "Rating (1-5): ");
        System.out.print("Comment (optional): ");
        String comment = scanner.nextLine();

        try {
            reviewService.createReview(bookId, memberId, rating, comment);
            System.out.println("Review created successfully.");
        } catch (RuntimeException e) {
            System.out.println("Could not create review: " + e.getMessage());
        }
    }

    private void sortBooks() {
        System.out.println("\n--- SORT BOOKS ---");
        System.out.println("1. By name");
        System.out.println("2. By author");
        System.out.println("3. By ID");

        int sortChoice = MenuInput.readInt(scanner, "Choose sorting option: ");
        String sortBy = switch (sortChoice) {
            case 1 -> "name";
            case 2 -> "author";
            case 3 -> "id";
            default -> "";
        };

        if (sortBy.isEmpty()) {
            System.out.println("Invalid sorting option.");
            return;
        }

        List<BookDTO> books = bookService.getBooksSortedBy(sortBy);
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("\n--- SORTED BOOKS ---");
        for (BookDTO book : books) {
            System.out.println(
                    "ID: " + book.getId()
                            + " | Title: " + book.getTitle()
                            + " | Authors: " + safeValue(book.getAuthors())
                            + " | Available copies: " + book.getAvailableCopies()
            );
        }
    }
}
