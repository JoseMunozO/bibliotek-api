package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.FineDAO;
import se.josecarlos.bibliotek.data.MemberDAO;
import se.josecarlos.bibliotek.dto.FineDTO;
import se.josecarlos.bibliotek.model.Fine;

import java.util.List;

public class FineService {

    private final FineDAO fineDAO;
    private final MemberDAO memberDAO;

    public FineService() {
        this.fineDAO = new FineDAO();
        this.memberDAO = new MemberDAO();
    }

    public List<FineDTO> getFinesByMemberId(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("Invalid member ID.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Member not found.");
        }

        return fineDAO.getFinesByMemberId(memberId);
    }

    public FineDTO payFine(int memberId, int fineId) {
        if (memberId <= 0 || fineId <= 0) {
            throw new ValidationException("Member ID and fine ID must be greater than 0.");
        }

        Fine fine = fineDAO.getFineByIdForMember(fineId, memberId);
        if (fine == null) {
            throw new NotFoundException("Fine not found for this member.");
        }

        if (FineDAO.STATUS_PAID.equalsIgnoreCase(fine.getStatus()) || !fineDAO.payFine(fineId)) {
            throw new ConflictException("Fine is already paid.");
        }

        return fineDAO.getFineDetailsById(fineId);
    }
}
