package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "matching_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"matching_campaign_id", "student_id", "project_id"},
                        name = "uk_result_campaign_student_project"
                ),
                @UniqueConstraint(
                        columnNames = {"matching_campaign_id", "student_id", "subject_id"},
                        name = "uk_result_campaign_student_subject"
                )
        },
        indexes = {
                @Index(name = "idx_result_campaign", columnList = "matching_campaign_id"),
                @Index(name = "idx_result_student", columnList = "student_id"),
                @Index(name = "idx_result_project", columnList = "project_id"),
                @Index(name = "idx_result_subject", columnList = "subject_id"),
                @Index(name = "idx_result_global_score", columnList = "global_score")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * Score partiel pour les intérêts / mots-clés (0.0 à 1.0)
     */
    @DecimalMin(value = "0.0", message = "Le score doit être au minimum 0")
    @DecimalMax(value = "1.0", message = "Le score doit être au maximum 1")
    @Column(name = "interests_score", precision = 5, scale = 4)
    private BigDecimal interestsScore;

    /**
     * Score partiel pour le type de travail (0.0 à 1.0)
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
     * Détail du score compétences
     * Ex: 2/4 compétences requises matchées
     */
    @Column(name = "skills_details", length = 500)
    private String skillsDetails;

    /**
     * Détail du score intérêts / mots-clés
     * Ex: 3/5 mots-clés matchés
     */
    @Column(name = "interests_details", length = 500)
    private String interestsDetails;

    /**
     * Rang de recommandation pour cet étudiant dans cette campagne
     */
    @Column(name = "recommendation_rank")
    private Integer recommendationRank;

    /**
     * Indique si l'étudiant est accepté dans ce projet/option
     * (dans les limites de la capacité maxStudents).
     * true  → dans les X meilleurs, place disponible
     * false → classé mais hors capacité
     */
    @Column(name = "accepted", nullable = false)
    @Builder.Default
    private Boolean accepted = false;

    /**
     * Algorithme utilisé pour ce calcul
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_used", nullable = false, length = 30)
    private MatchingAlgorithmType algorithmUsed;

    @CreationTimestamp
    @Column(name = "calculation_date", nullable = false, updatable = false)
    private LocalDateTime calculationDate;

    /**
     * Campagne de matching concernée
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_campaign_id", nullable = false)
    private MatchingCampaign matchingCampaign;

    /**
     * Étudiant concerné par ce résultat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Projet concerné par ce résultat
     * Utilisé uniquement si la campagne est de type PROJECT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Matière / option concernée par ce résultat
     * Utilisé uniquement si la campagne est de type SUBJECT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
}