package se.josecarlos.bibliotek.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import se.josecarlos.bibliotek.business.LoanService;
import se.josecarlos.bibliotek.business.MemberService;
import se.josecarlos.bibliotek.dto.LoanDTO;
import se.josecarlos.bibliotek.dto.MemberDTO;
import se.josecarlos.bibliotek.dto.MemberProfileDTO;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;

    public MemberController(MemberService memberService, LoanService loanService) {
        this.memberService = memberService;
        this.loanService = loanService;
    }

    @GetMapping
    public List<MemberDTO> getMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public MemberProfileDTO getMemberProfile(@PathVariable int id) {
        return memberService.getMemberProfile(id);
    }

    @GetMapping("/{id}/loans")
    public List<LoanDTO> getMemberLoans(@PathVariable int id) {
        return loanService.getLoansByMemberId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDTO registerMember(@RequestBody MemberRequest request) {
        return memberService.registerMember(request.firstName(), request.lastName(), request.email());
    }

    @PutMapping("/{id}")
    public MemberDTO updateMember(@PathVariable int id, @RequestBody MemberRequest request) {
        return memberService.updateMember(id, request.firstName(), request.lastName(),
                request.email(), request.membershipType());
    }

    @PostMapping("/{id}/suspend")
    public MemberDTO suspendMember(@PathVariable int id) {
        return memberService.suspendMember(id);
    }
}
