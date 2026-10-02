package se.josecarlos.bibliotek.mapper;

import se.josecarlos.bibliotek.dto.MemberDTO;
import se.josecarlos.bibliotek.model.Member;

public class MemberMapper {

    public static MemberDTO toDTO(Member member) {
        String fullName = member.getFirstName() + " " + member.getLastName();

        return new MemberDTO(
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                fullName,
                member.getEmail(),
                member.getMembershipType(),
                member.getStatus()
        );
    }
}
