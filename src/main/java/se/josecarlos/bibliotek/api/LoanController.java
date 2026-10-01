package se.josecarlos.bibliotek.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import se.josecarlos.bibliotek.business.LoanService;
import se.josecarlos.bibliotek.dto.LoanDTO;
import se.josecarlos.bibliotek.dto.LoanReturnDTO;
import se.josecarlos.bibliotek.dto.OverdueLoanDTO;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanDTO> getActiveLoans() {
        return loanService.getActiveLoans();
    }

    @GetMapping("/overdue")
    public List<OverdueLoanDTO> getOverdueLoans() {
        return loanService.getOverdueLoanRegister();
    }

    @GetMapping("/{id}")
    public LoanDTO getLoan(@PathVariable int id) {
        return loanService.getLoan(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDTO borrowBook(@RequestBody LoanRequest request) {
        return loanService.borrowBook(request.memberId(), request.bookId());
    }

    @PostMapping("/{id}/return")
    public LoanReturnDTO returnBook(@PathVariable int id) {
        return loanService.returnBook(id);
    }

    @PostMapping("/{id}/extend")
    public LoanDTO extendLoan(@PathVariable int id, @RequestBody ExtendLoanRequest request) {
        return loanService.extendLoan(id, request.extraDays());
    }
}
