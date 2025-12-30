package fr.amu.bestchoice.web.dto.auth;

/**
 * DTO de réponse après une inscription réussie
 * Retourné par POST /api/auth/register
 */
public record RegisterResponse(

        Long userId,
        String email,
        String firstName,
        String lastName,

        // Message de confirmation
        String message
) {

    public RegisterResponse(Long userId, String email, String firstName, String lastName) {
        this(userId, email, firstName, lastName,
                "Inscription réussie > vous pouvez maintenant vous connecter");
    }
}
