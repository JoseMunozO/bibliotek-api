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
            throw new ValidationException("ID de socio no válido.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Socio no encontrado.");
        }

        return fineDAO.getFinesByMemberId(memberId);
    }

    public FineDTO payFine(int memberId, int fineId) {
        if (memberId <= 0 || fineId <= 0) {
            throw new ValidationException("El ID del socio y el de la multa deben ser mayores que 0.");
        }

        Fine fine = fineDAO.getFineByIdForMember(fineId, memberId);
        if (fine == null) {
            throw new NotFoundException("No se ha encontrado esa multa para este socio.");
        }

        if (FineDAO.STATUS_PAID.equalsIgnoreCase(fine.getStatus()) || !fineDAO.payFine(fineId)) {
            throw new ConflictException("La multa ya está pagada.");
        }

        return fineDAO.getFineDetailsById(fineId);
    }
}
