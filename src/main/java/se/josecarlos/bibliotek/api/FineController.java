package se.josecarlos.bibliotek.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.josecarlos.bibliotek.business.FineService;
import se.josecarlos.bibliotek.dto.FineDTO;

import java.util.List;

@RestController
@RequestMapping("/api/members/{memberId}/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public List<FineDTO> getFines(@PathVariable int memberId) {
        return fineService.getFinesByMemberId(memberId);
    }

    @PostMapping("/{fineId}/pay")
    public FineDTO payFine(@PathVariable int memberId, @PathVariable int fineId) {
        return fineService.payFine(memberId, fineId);
    }
}
