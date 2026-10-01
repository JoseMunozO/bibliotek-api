package se.josecarlos.bibliotek.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.josecarlos.bibliotek.business.BookService;
import se.josecarlos.bibliotek.dto.BookDTO;
import se.josecarlos.bibliotek.dto.BookDetailsDTO;
import se.josecarlos.bibliotek.dto.BookStatisticsDTO;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /api/books?search=harry  |  ?available=true  |  ?sort=author
    @GetMapping
    public List<BookDTO> getBooks(@RequestParam(required = false) String search,
                                  @RequestParam(defaultValue = "false") boolean available,
                                  @RequestParam(required = false) String sort) {
        if (search != null) {
            return bookService.searchBooks(search);
        }
        if (available) {
            return bookService.getAvailableBooks();
        }
        if (sort != null) {
            return bookService.getBooksSortedBy(sort);
        }
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookDetailsDTO getBookDetails(@PathVariable int id) {
        return bookService.getBookDetails(id);
    }

    @GetMapping("/most-borrowed")
    public List<BookStatisticsDTO> getMostBorrowedBooks(@RequestParam(defaultValue = "10") int limit) {
        return bookService.getMostBorrowedBooks(limit);
    }
}
