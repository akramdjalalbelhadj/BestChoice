package fr.amu.bestchoice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant le résultat du matching entre un étudiant et un projet.
 *
 * Stocke les scores calculés par l'algorithme de Weighted Matching :
 * - Score global de compatibilité
 * - Scores partiels par critère (compétences, intérêts, type de travail)
 * - Détails de calcul pour la transparence
 */
@Entity
@Table(
        name = "matching_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"student_id", "project_id", "matching_session_id"},
                        name = "uk_result_student_project_session"
                )
        },
        indexes = {
                @Index(name = "idx_result_student", columnList = "student_id"),
                @Index(name = "idx_result_project", columnList = "project_id"),
                @Index(name = "idx_result_global_score", columnList = "global_score"),
                @Index(name = "idx_result_session", columnList = "matching_session_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MatchingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant de la session de matching (pour regrouper les résultats)
     */
    @Column(name = "matching_session_id", nullable = false, length = 50)
    private String sessionId;

    /**
     * Score global de compatibilité (0.0 à 1.0)
     */
    @DecimalMin(value = "0.0", message = "Le score doit être au minimum 0")
    @DecimalMax(value = "1.0", message = "Le score doit être au maximum 1")
    @Column(name = "global_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal globalScore;

    /**
     * Score partiel pour les compétences (0.0 à 1.0)
     */
    @DecimalMin(value = "0.0", message = "Le score doit être au minimum 0")
    @DecimalMax(value = "1.0", message = "Le score doit être au maximum 1")
    @Column(name = "skills_score", precision = 5, scale = 4)
    private BigDecimal skillsScore;

    /**
     * Score partiel pour les intérêts/mots-clés (0.0 à 1.0)
     */
    @DecimalMin(value = "0.0", message = "Le score doit être au minimum 0")
    @DecimalMax(value = "1.0", message = "Le score doit être au maximum 1")
    @Column(name = "interests_score", precision = 5, scale = 4)
    private BigDecimal interestsScore;

    /**
     * Score partiel pour le type de travail (0.0 ou 1.0)
     */
    @DecimalMin(value = "0.0", message = "Le score doit être au minimum 0")
    @DecimalMax(value = "1.0", message = "Le score doit être au maximum 1")
    @Column(name = "work_type_score", precision = 5, scale = 4)
    private BigDecimal workTypeScore;

    /**
     * Poids utilisé pour les compétences lors du calcul
     */
    @Column(name = "skills_weight", precision = 4, scale = 2)
    private BigDecimal skillsWeight;

    /**
     * Poids utilisé pour les intérêts lors du calcul
     */
    @Column(name = "interests_weight", precision = 4, scale = 2)
    private BigDecimal interestsWeight;

    /**
     * Poids utilisé pour le type de travail lors du calcul
     */
    @Column(name = "work_type_weight", precision = 4, scale = 2)
    private BigDecimal workTypeWeight;

    /**
     * Nombre de compétences matchées / nombre total de prérequis
     */
    @Column(name = "skills_details", length = 500)
    private String skillsDetails;

    /**
     * Nombre d'intérêts matchés / nombre total de mots-clés
     */
    @Column(name = "interests_details", length = 500)
    private String interestsDetails;

    /**
     * Rang du projet dans les recommandations pour cet étudiant
     */
    @Column(name = "recommendation_rank")
    private Integer recommendationRank;

    /**
     * Indique si ce résultat répond au seuil minimum de pertinence
     */
    @Column(name = "above_threshold", nullable = false)
    @Builder.Default
    private Boolean aboveThreshold = true;

    /**
     * Seuil minimum utilisé lors du calcul
     */
    @Column(name = "threshold_used", precision = 4, scale = 2)
    private BigDecimal thresholdUsed;

    /**
     * Algorithme utilisé pour ce calcul
     */
    @Column(name = "algorithm_used", length = 50)
    @Builder.Default
    private String algorithmUsed = "WEIGHTED_MATCHING";

    @CreationTimestamp
    @Column(name = "calculation_date", nullable = false, updatable = false)
    private LocalDateTime calculationDate;

    /**
     * Étudiant concerné par ce résultat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Projet concerné par ce résultat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


}
