package online.kancl.page.registration;

public record Registration(
        String username,
        String password,
        String passwordCheck,
        String email
) {
}
