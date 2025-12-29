package fr.amu.bestchoice.web.exception;

/**
 * Exception levée lorsqu'une ressource demandée n'existe pas.
 *
 * Utilisation typique :
 *  - utilisateur inexistant
 *  - projet introuvable
 *  - étudiant non trouvé
 **/

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
