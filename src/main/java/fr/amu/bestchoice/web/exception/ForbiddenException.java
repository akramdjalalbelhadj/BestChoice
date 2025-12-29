package fr.amu.bestchoice.web.exception;

/**
 * Exception levée lorsqu'un utilisateur est authentifié
 * mais ne dispose pas des droits nécessaires.
 * Correspond à une erreur HTTP 403 (Forbidden).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
