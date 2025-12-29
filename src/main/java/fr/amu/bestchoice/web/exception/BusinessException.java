package fr.amu.bestchoice.web.exception;

/**
 * Exception représentant une règle métier violée.
 *
 * Elle est utilisée lorsque :
 *  - une action est invalide selon les règles fonctionnelles
 *  - mais que la requête est techniquement correcte
 *
 * Exemples :
 *  - un étudiant essaie de choisir plus de préférences que permis
 *  - un projet est déjà complet
 *  - un rang de préférence est déjà utilisé
 *
 * Cette exception correspond généralement à une erreur HTTP 400 (Bad Request).
 */

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
