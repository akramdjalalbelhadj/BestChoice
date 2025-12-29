package fr.amu.bestchoice.model.enums;

/**
 * Énumération des types de travail possibles pour les projets.
 */
public enum WorkType  {

    DEVELOPPEMENT("Développement logiciel"),
    RECHERCHE("Recherche théorique"),
    ANALYSE("Analyse de données"),
    VEILLE("Veille technologique"),
    CONCEPTION("Conception et modélisation"),
    DOCUMENTATION("Documentation technique"),
    TEST("Tests et qualité logicielle"),
    MIXTE("Travail mixte");

    private final String description;

    WorkType (String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
