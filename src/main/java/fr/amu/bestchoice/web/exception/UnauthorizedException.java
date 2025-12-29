package fr.amu.bestchoice.web.exception;

/**
 * Exception levée lorsqu'un utilisateur n'est pas authentifié
 *
 * Cas d'utilisation :
 *  - accès à une ressource protégée sans être connecté
 *  - token JWT manquant ou invalide
 *
 * Correspond à une erreur HTTP 401 (Unauthorized)
 */

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
