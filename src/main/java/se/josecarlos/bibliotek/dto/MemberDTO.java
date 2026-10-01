package se.josecarlos.bibliotek.dto;

public class MemberDTO {

    private int id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String membershipType;
    private String status;

    public MemberDTO(int id, String firstName, String lastName, String fullName, String email,
                     String membershipType, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.membershipType = membershipType;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public String getStatus() {
        return status;
    }
}
