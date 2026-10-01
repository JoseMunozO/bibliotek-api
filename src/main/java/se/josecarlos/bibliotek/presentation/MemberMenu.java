package se.josecarlos.bibliotek.presentation;

import se.josecarlos.bibliotek.business.MemberService;
import se.josecarlos.bibliotek.dto.MemberDTO;
import se.josecarlos.bibliotek.dto.MemberProfileDTO;

import java.util.List;
import java.util.Scanner;

public class MemberMenu {

    private final MemberService memberService;
    private final Scanner scanner;

    public MemberMenu() {
        this.memberService = new MemberService();
        this.scanner = new Scanner(System.in);
    }

    public void showMemberMenu() {
        int choice;

        do {
            System.out.println("\n--- MEMBER MENU ---");
            System.out.println("1. Show all members");
            System.out.println("2. Register new member");
            System.out.println("3. Show member profile");
            System.out.println("4. Update member");
            System.out.println("5. Suspend member");
            System.out.println("0. Back");
            choice = MenuInput.readInt(scanner, "Choose: ");

            switch (choice) {
                case 1 -> showMembers();
                case 2 -> registerMember();
                case 3 -> showMemberProfile();
                case 4 -> updateMember();
                case 5 -> suspendMember();
                case 0 -> System.out.println("Back to main menu...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    public void showUserProfileMenu() {
        int choice;

        do {
            System.out.println("\n--- USER PROFILE MENU ---");
            System.out.println("1. Show member profile");
            System.out.println("2. Update member");
            System.out.println("0. Back");
            choice = MenuInput.readInt(scanner, "Choose: ");

            switch (choice) {
                case 1 -> showMemberProfile();
                case 2 -> updateMember();
                case 0 -> System.out.println("Back to user menu...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    private void showMembers() {
        List<MemberDTO> members = memberService.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        for (MemberDTO m : members) {
            System.out.println(
                    m.getId() + " | " + m.getFullName() + " | " + m.getEmail()
                            + " | Type: " + m.getMembershipType()
                            + " | Status: " + m.getStatus()
            );
        }
    }

    private void registerMember() {
        String firstName = MenuInput.readRequiredString(scanner, "First name: ");

        String lastName = MenuInput.readRequiredString(scanner, "Last name: ");

        String email = MenuInput.readRequiredString(scanner, "Email: ");

        try {
            MemberDTO created = memberService.registerMember(firstName, lastName, email);
            System.out.println("Member created with ID " + created.getId() + "!");
        } catch (RuntimeException e) {
            System.out.println("Could not create member: " + e.getMessage());
        }
    }

    private void showMemberProfile() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        MemberProfileDTO profile;

        try {
            profile = memberService.getMemberProfile(memberId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\n--- MEMBER PROFILE ---");
        System.out.println("ID: " + profile.getId());
        System.out.println("Name: " + profile.getFullName());
        System.out.println("Email: " + profile.getEmail());
        System.out.println("Membership date: " + profile.getMembershipDate());
        System.out.println("Membership type: " + profile.getMembershipType());
        System.out.println("Status: " + profile.getStatus());
        System.out.println("Active loans: " + profile.getActiveLoansCount());
        System.out.println("Total loans: " + profile.getTotalLoansCount());
        System.out.println("Total fines: " + profile.getTotalFinesCount());
        System.out.println("Unpaid fine amount: " + profile.getUnpaidFineAmount());
    }

    private void updateMember() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        String firstName = MenuInput.readRequiredString(scanner, "First name: ");
        String lastName = MenuInput.readRequiredString(scanner, "Last name: ");
        String email = MenuInput.readRequiredString(scanner, "Email: ");
        String membershipType = MenuInput.readRequiredString(scanner, "Membership type: ");

        try {
            memberService.updateMember(memberId, firstName, lastName, email, membershipType);
            System.out.println("Member updated successfully.");
        } catch (RuntimeException e) {
            System.out.println("Could not update member: " + e.getMessage());
        }
    }

    private void suspendMember() {
        int memberId = MenuInput.readPositiveInt(scanner, "Member ID: ");
        try {
            memberService.suspendMember(memberId);
            System.out.println("Member suspended successfully.");
        } catch (RuntimeException e) {
            System.out.println("Could not suspend member: " + e.getMessage());
        }
    }
}
