package se.josecarlos.bibliotek.presentation;

import se.josecarlos.bibliotek.business.FineService;
import se.josecarlos.bibliotek.business.LoanService;
import se.josecarlos.bibliotek.business.NotificationService;
import se.josecarlos.bibliotek.dto.LoanDTO;
import se.josecarlos.bibliotek.dto.LoanReturnDTO;
import se.josecarlos.bibliotek.dto.NotificationDTO;
import se.josecarlos.bibliotek.dto.OverdueLoanDTO;
import se.josecarlos.bibliotek.model.Fine;

import java.util.List;
import java.util.Scanner;

public class LoanMenu {

    private final LoanService loanService;
    private final FineService fineService;
    private final NotificationService notificationService;
    private final Scanner scanner;

    public LoanMenu() {
        this.loanService = new LoanService();
        this.fineService = new FineService();
        this.notificationService = new NotificationService();
        this.scanner = new Scanner(System.in);
    }

    public void showLoanMenu() {
        int choice;

        do {
            System.out.println("\n--- LOAN MENU ---");
            System.out.println("1. Borrow book");
            System.out.println("2. Return book");
            System.out.println("3. Show active loans");
            System.out.println("4. Show loans by member");
            System.out.println("5. Show overdue loans");
            System.out.println("6. Show fines by member");
            System.out.println("7. Pay fine");
            System.out.println("8. Extend loan");
            System.out.println("9. Show overdue register");
            System.out.println("10. Send notification");
            System.out.println("11. Show notifications by member");
            System.out.println("0. Back");
            choice = MenuInput.readInt(scanner, "Choose: ");

            switch (choice) {
                case 1 -> borrowBook();
                case 2 -> returnBook();
                case 3 -> showActiveLoans();
                case 4 -> showLoansByMember();
                case 5 -> showOverdueLoans();
                case 6 -> showFinesByMember();
                case 7 -> payFine();
                case 8 -> extendLoan();
                case 9 -> showOverdueRegister();
                case 10 -> sendNotification();
                case 11 -> showNotificationsByMember();
                case 0 -> System.out.println("Back to main menu...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    public void showUserLoanMenu() {
        int choice;

        do {
            System.out.println("\n--- USER LOAN MENU ---");
            System.out.println("1. Show loans by member");
            System.out.println("2. Show fines by member");
            System.out.println("3. Show notifications by member");
            System.out.println("0. Back");
            choice = MenuInput.readInt(scanner, "Choose: ");

            switch (choice) {
                case 1 -> showLoansByMember();
                case 2 -> showFinesByMember();
                case 3 -> showNotificationsByMember();
                case 0 -> System.out.println("Back to role menu...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    private void borrowBook() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        int bookId = MenuInput.readPositiveInt(scanner, "Book ID: ");

        try {
            LoanDTO loan = loanService.borrowBook(memberId, bookId);
            System.out.println("Book borrowed successfully. Loan ID: " + loan.getId() + ", due date: " + loan.getDueDate());
        } catch (RuntimeException e) {
            System.out.println("Could not borrow book: " + e.getMessage());
        }
    }


    private void showActiveLoans() {
        List<LoanDTO> loans = loanService.getActiveLoans();

        if (loans.isEmpty()) {
            System.out.println("No active loans found.");
            return;
        }

        for (LoanDTO loan : loans) {
            System.out.println(
                    "Loan ID: " + loan.getId()
                            + " | Book ID: " + loan.getBookId()
                            + " | Member ID: " + loan.getMemberId()
                            + " | Loan date: " + loan.getLoanDate()
                            + " | Due date: " + loan.getDueDate()
            );
        }
    }

    private void showLoansByMember() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");

        List<LoanDTO> loans;

        try {
            loans = loanService.getLoansByMemberId(memberId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        if (loans.isEmpty()) {
            System.out.println("No loans found for this member.");
            return;
        }

        for (LoanDTO loan : loans) {
            String status = loan.getReturnDate() == null ? "ACTIVE" : "RETURNED";

            System.out.println(
                    "Loan ID: " + loan.getId()
                            + " | Book ID: " + loan.getBookId()
                            + " | Loan date: " + loan.getLoanDate()
                            + " | Due date: " + loan.getDueDate()
                            + " | Return date: " + loan.getReturnDate()
                            + " | Status: " + status
            );
        }
    }

    private void showOverdueLoans() {
        List<LoanDTO> loans = loanService.getOverdueLoans();

        if (loans.isEmpty()) {
            System.out.println("No overdue loans found.");
            return;
        }

        for (LoanDTO loan : loans) {
            System.out.println(
                    "Loan ID: " + loan.getId()
                            + " | Book ID: " + loan.getBookId()
                            + " | Member ID: " + loan.getMemberId()
                            + " | Due date: " + loan.getDueDate()
                            + " | Status: OVERDUE"
            );
        }
    }

    private void showFinesByMember() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");

        List<Fine> fines = fineService.getFinesByMemberId(memberId);

        if (fines.isEmpty()) {
            System.out.println("No fines found for this member.");
            return;
        }

        for (Fine fine : fines) {
            System.out.println(
                    "Fine ID: " + fine.getId()
                            + " | Loan ID: " + fine.getLoanId()
                            + " | Amount: " + fine.getAmount()
                            + " | Issued date: " + fine.getIssuedDate()
                            + " | Status: " + fine.getStatus()
            );
        }
    }

    private void payFine() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        int fineId = MenuInput.readPositiveInt(scanner, "Fine ID: ");

        boolean success = fineService.payFine(memberId, fineId);

        if (success) {
            System.out.println("Fine paid successfully.");
        } else {
            System.out.println("Could not pay fine.");
        }
    }

    private void returnBook() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        int bookId = MenuInput.readPositiveInt(scanner, "Book ID: ");

        try {
            LoanReturnDTO result = loanService.returnBookByMemberAndBook(memberId, bookId);
            System.out.println("Book returned successfully.");
            if (result.getFineAmount() > 0) {
                System.out.println("The book was returned late. Fine: " + result.getFineAmount());
            }
        } catch (RuntimeException e) {
            System.out.println("Could not return book: " + e.getMessage());
        }
    }

    private void extendLoan() {
        int loanId = MenuInput.readPositiveInt(scanner, "Loan ID: ");
        int extraDays = MenuInput.readPositiveInt(scanner, "Extra days: ");

        try {
            LoanDTO loan = loanService.extendLoan(loanId, extraDays);
            System.out.println("Loan extended successfully. New due date: " + loan.getDueDate());
        } catch (RuntimeException e) {
            System.out.println("Could not extend loan: " + e.getMessage());
        }
    }

    private void showOverdueRegister() {
        List<OverdueLoanDTO> overdueLoans = loanService.getOverdueLoanRegister();

        if (overdueLoans.isEmpty()) {
            System.out.println("No overdue loans found.");
            return;
        }

        for (OverdueLoanDTO loan : overdueLoans) {
            System.out.println(
                    "Loan ID: " + loan.getLoanId()
                            + " | Book ID: " + loan.getBookId()
                            + " | Title: " + loan.getBookTitle()
                            + " | Member ID: " + loan.getMemberId()
                            + " | Member: " + loan.getMemberName()
                            + " | Email: " + loan.getMemberEmail()
                            + " | Due date: " + loan.getDueDate()
            );
        }
    }

    private void sendNotification() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        int loanIdInput = MenuInput.readInt(scanner, "Loan ID (0 if not linked to a loan): ");
        String type = MenuInput.readRequiredString(scanner, "Notification type: ");
        String message = MenuInput.readRequiredString(scanner, "Message: ");

        Integer loanId = loanIdInput <= 0 ? null : loanIdInput;
        boolean success = notificationService.sendNotification(memberId, loanId, type, message);

        if (success) {
            System.out.println("Notification sent successfully.");
        } else {
            System.out.println("Could not send notification.");
        }
    }

    private void showNotificationsByMember() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        List<NotificationDTO> notifications = notificationService.getNotificationsByMemberId(memberId);

        if (notifications.isEmpty()) {
            System.out.println("No notifications found for this member.");
            return;
        }

        System.out.println("\n--- NOTIFICATIONS ---");
        for (NotificationDTO notification : notifications) {
            System.out.println(
                    "Notification ID: " + notification.getId()
                            + " | Loan ID: " + (notification.getLoanId() != null ? notification.getLoanId() : "N/A")
                            + " | Type: " + notification.getType()
                            + " | Date: " + notification.getSentDate()
                            + " | Read: " + notification.isRead()
            );
            System.out.println("Message: " + notification.getMessage());
        }
    }
}
