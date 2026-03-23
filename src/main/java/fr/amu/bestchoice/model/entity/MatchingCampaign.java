package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "matching_campaigns",
        indexes = {
                @Index(name = "idx_matching_campaign_teacher", columnList = "teacher_id"),
                @Index(name = "idx_matching_campaign_algorithm", columnList = "algorithm_type"),
                @Index(name = "idx_matching_campaign_type", columnList = "campaign_type"),
                @Index(name = "idx_matching_campaign_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la campagne
     * Ex: Matching Master IDL 2025-2026 S2
     */
    @NotBlank(message = "Le nom de la campagne est obligatoire")
    @Size(max = 150, message = "Le nom de la campagne ne doit pas dépasser 150 caractères")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Description libre de la campagne
     */
    @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Année universitaire
     * Ex: 2025-2026
     */
    @Size(max = 9, message = "L'année universitaire doit être au format YYYY-YYYY")
    @Column(name = "academic_year", length = 9)
    private String academicYear;

    /**
     * Semestre concerné
     */
    @Column(name = "semester")
    private Integer semester;

    /**
     * Type de campagne
     * PROJECT ou SUBJECT
     */
    @NotNull(message = "Le type de campagne est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 20)
    private MatchingCampaignType campaignType;

    /**
     * Algorithme utilisé pour la campagne
     * STABLE / WEIGHTED / HYBRID
     */
    @NotNull(message = "Le type d'algorithme est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm_type", nullable = false, length = 30)
    private MatchingAlgorithmType algorithmType;

    /**
     * Poids des compétences
     */
    @NotNull(message = "Le poids des compétences est obligatoire")
    @DecimalMin(value = "0.0", message = "Le poids doit être >= 0")
    @DecimalMax(value = "1.0", message = "Le poids doit être <= 1")
    @Column(name = "skills_weight", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal skillsWeight = new BigDecimal("0.33");

    /**
     * Poids du type de travail recherché
     */
    @NotNull(message = "Le poids du type de travail est obligatoire")
    @DecimalMin(value = "0.0", message = "Le poids doit être >= 0")
    @DecimalMax(value = "1.0", message = "Le poids doit être <= 1")
    @Column(name = "work_type_weight", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal workTypeWeight = new BigDecimal("0.33");

    /**
     * Poids des centres d'intérêt / mots-clés / projection future
     */
    @NotNull(message = "Le poids des intérêts est obligatoire")
    @DecimalMin(value = "0.0", message = "Le poids doit être >= 0")
    @DecimalMax(value = "1.0", message = "Le poids doit être <= 1")
    @Column(name = "interests_weight", nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal interestsWeight = new BigDecimal("0.34");

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enseignant créateur / responsable de la campagne
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Étudiants participant à cette campagne
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "matching_campaign_students",
            joinColumns = @JoinColumn(name = "matching_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    /**
     * Projets participant à cette campagne
     * Utilisé uniquement si campaignType = PROJECT
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "matching_campaign_projects",
            joinColumns = @JoinColumn(name = "matching_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    /**
     * Matières / options participant à cette campagne
     * Utilisé uniquement si campaignType = SUBJECT
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "matching_campaign_subjects",
            joinColumns = @JoinColumn(name = "matching_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private Set<Subject> subjects = new HashSet<>();

    /**
     * Préférences étudiantes exprimées dans cette campagne
     */
    @OneToMany(mappedBy = "matchingCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<StudentPreference> studentPreferences = new HashSet<>();

    /**
     * Résultats de matching calculés dans cette campagne
     */
    @OneToMany(mappedBy = "matchingCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MatchingResult> matchingResults = new HashSet<>();
}