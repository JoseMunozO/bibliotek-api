package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.BookDAO;
import se.josecarlos.bibliotek.data.LoanDAO;
import se.josecarlos.bibliotek.data.MemberDAO;
import se.josecarlos.bibliotek.data.ReviewDAO;
import se.josecarlos.bibliotek.dto.ReviewDTO;

import java.util.List;

public class ReviewService {

    private final ReviewDAO reviewDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final LoanDAO loanDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.loanDAO = new LoanDAO();
    }

    public List<ReviewDTO> getReviewsByBookId(int bookId) {
        if (bookId <= 0) {
            throw new ValidationException("ID de libro no válido.");
        }

        if (bookDAO.getBookById(bookId) == null) {
            throw new NotFoundException("Libro no encontrado.");
        }

        return reviewDAO.getReviewsByBookId(bookId);
    }

    public ReviewDTO createReview(int bookId, int memberId, int rating, String comment) {
        String normalizedComment = comment == null ? "" : comment.trim();

        if (bookId <= 0 || memberId <= 0) {
            throw new ValidationException("El ID del libro y el del socio deben ser mayores que 0.");
        }

        if (rating < 1 || rating > 5) {
            throw new ValidationException("La puntuación debe estar entre 1 y 5.");
        }

        if (bookDAO.getBookById(bookId) == null) {
            throw new NotFoundException("Libro no encontrado.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Socio no encontrado.");
        }

        if (!loanDAO.hasReturnedLoanForBookAndMember(bookId, memberId)) {
            throw new ConflictException("El socio debe haber devuelto este libro antes de reseñarlo.");
        }

        if (reviewDAO.hasMemberReviewedBook(memberId, bookId)) {
            throw new ConflictException("Este socio ya ha reseñado este libro.");
        }

        int reviewId = reviewDAO.createReview(bookId, memberId, rating, normalizedComment);
        return reviewDAO.getReviewById(reviewId);
    }
}
