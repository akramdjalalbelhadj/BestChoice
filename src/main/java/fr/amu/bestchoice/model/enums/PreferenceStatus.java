package fr.amu.bestchoice.model.enums;

// ==================== Enums ====================

/**
 * Statuts possibles d'une préférence
 */
public enum PreferenceStatus {
    PENDING("En attente de traitement"),
    ACCEPTED("Préférence acceptée"),
    REJECTED("Préférence refusée"),
    CANCELLED("Annulée par l'étudiant");

    private final String description;

    PreferenceStatus(String description) {this.description = description;}
    public String getDescription() {return description;}
}