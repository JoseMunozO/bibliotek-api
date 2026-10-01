package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.MemberDAO;
import se.josecarlos.bibliotek.dto.MemberDTO;
import se.josecarlos.bibliotek.dto.MemberProfileDTO;
import se.josecarlos.bibliotek.mapper.MemberMapper;
import se.josecarlos.bibliotek.model.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class MemberService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final MemberDAO memberDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
    }

    public List<MemberDTO> getAllMembers() {
        return memberDAO.getAllMembers().stream()
                .map(MemberMapper::toDTO)
                .toList();
    }

    public MemberDTO getMember(int memberId) {
        return MemberMapper.toDTO(findMember(memberId));
    }

    public MemberDTO registerMember(String firstName, String lastName, String email) {
        String normalizedFirstName = normalize(firstName);
        String normalizedLastName = normalize(lastName);
        String normalizedEmail = normalize(email).toLowerCase();

        if (normalizedFirstName.isEmpty() || normalizedLastName.isEmpty() || normalizedEmail.isEmpty()) {
            throw new ValidationException("First name, last name and email are required.");
        }

        validateEmail(normalizedEmail);

        if (memberDAO.existsByEmail(normalizedEmail)) {
            throw new ConflictException("A member with this email already exists.");
        }

        Member member = new Member(
                0,
                normalizedFirstName,
                normalizedLastName,
                normalizedEmail,
                LocalDate.now(),
                "standard",
                "ACTIVE"
        );
        int id = memberDAO.createMember(member);
        return getMember(id);
    }

    public MemberProfileDTO getMemberProfile(int memberId) {
        validateId(memberId);

        MemberProfileDTO profile = memberDAO.getMemberProfile(memberId);
        if (profile == null) {
            throw new NotFoundException("Member not found.");
        }

        return profile;
    }

    public MemberDTO updateMember(int memberId, String firstName, String lastName, String email, String membershipType) {
        String normalizedFirstName = normalize(firstName);
        String normalizedLastName = normalize(lastName);
        String normalizedEmail = normalize(email).toLowerCase();
        String normalizedMembershipType = normalize(membershipType).toLowerCase();

        findMember(memberId);

        if (normalizedFirstName.isEmpty() || normalizedLastName.isEmpty() || normalizedEmail.isEmpty() || normalizedMembershipType.isEmpty()) {
            throw new ValidationException("First name, last name, email and membership type are required.");
        }

        validateEmail(normalizedEmail);

        if (memberDAO.existsByEmailExcludingMember(normalizedEmail, memberId)) {
            throw new ConflictException("A member with this email already exists.");
        }

        memberDAO.updateMember(memberId, normalizedFirstName, normalizedLastName, normalizedEmail, normalizedMembershipType);
        return getMember(memberId);
    }

    public MemberDTO suspendMember(int memberId) {
        Member member = findMember(memberId);

        if ("SUSPENDED".equalsIgnoreCase(member.getStatus())) {
            throw new ConflictException("Member is already suspended.");
        }

        memberDAO.updateStatus(memberId, "SUSPENDED");
        return getMember(memberId);
    }

    private Member findMember(int memberId) {
        validateId(memberId);

        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found.");
        }

        return member;
    }

    private void validateId(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("Invalid member ID.");
        }
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
