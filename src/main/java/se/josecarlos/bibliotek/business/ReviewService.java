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
            throw new ValidationException("Invalid book ID.");
        }

        if (bookDAO.getBookById(bookId) == null) {
            throw new NotFoundException("Book not found.");
        }

        return reviewDAO.getReviewsByBookId(bookId);
    }

    public ReviewDTO createReview(int bookId, int memberId, int rating, String comment) {
        String normalizedComment = comment == null ? "" : comment.trim();

        if (bookId <= 0 || memberId <= 0) {
            throw new ValidationException("Book ID and member ID must be greater than 0.");
        }

        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5.");
        }

        if (bookDAO.getBookById(bookId) == null) {
            throw new NotFoundException("Book not found.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Member not found.");
        }

        if (!loanDAO.hasReturnedLoanForBookAndMember(bookId, memberId)) {
            throw new ConflictException("The member must have returned this book before leaving a review.");
        }

        if (reviewDAO.hasMemberReviewedBook(memberId, bookId)) {
            throw new ConflictException("This member has already reviewed this book.");
        }

        int reviewId = reviewDAO.createReview(bookId, memberId, rating, normalizedComment);
        return reviewDAO.getReviewById(reviewId);
    }
}
