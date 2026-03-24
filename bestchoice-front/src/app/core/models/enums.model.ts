export enum Role {
  STUDENT = 'ETUDIANT',
  TEACHER = 'ENSEIGNANT',
  ADMIN = 'ADMIN'
}

export enum WorkType {
  DEVELOPPEMENT = 'DEVELOPPEMENT',
  RECHERCHE = 'RECHERCHE',
  ANALYSE = 'ANALYSE',
  CONCEPTION = 'CONCEPTION'
}

export enum PreferenceStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED'
}

/**
 * Types d'algorithmes supportés par le moteur de matching.
 * Doit correspondre à MatchingAlgorithmType.java
 */
export enum MatchingAlgorithmType {
  /** Algorithme de Gale-Shapley (Mariages stables) */
  STABLE = 'STABLE',

  /** Calcul basé uniquement sur les scores de similarité (Pondéré) */
  WEIGHTED = 'WEIGHTED',

  /** Combinaison des deux approches */
  HYBRID = 'HYBRID'
}
