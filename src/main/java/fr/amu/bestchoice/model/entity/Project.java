package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table( name = "projects",
        indexes = {
                @Index(name = "idx_project_teacher", columnList = "teacher_id"),
                @Index(name = "idx_project_work_type", columnList = "work_type"),
                @Index(name = "idx_project_active", columnList = "active")
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre du projet est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "La description du projet est obligatoire")
    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    /**
     * Objectifs pédagogiques du projet
     */
    @Size(max = 2000, message = "Les objectifs ne doivent pas dépasser 2000 caractères")
    @Column(name = "objectives", length = 2000)
    private String objectives;

    /**
     * Type de travail principal requis
     */
    @NotNull(message = "Le type de travail est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false, length = 20)
    private WorkType workType;

    /**
     * Nombre de crédits ECTS associés
     */
    @Min(value = 1, message = "Le nombre de crédits doit être au minimum 1")
    @Max(value = 30, message = "Le nombre de crédits ne peut pas dépasser 30")
    @Column(name = "credits")
    private Integer credits;

    /**
     * Durée estimée en semaines
     */
    @Min(value = 1, message = "La durée doit être au minimum 1 semaine")
    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    /**
     * Nombre maximum d'étudiants pouvant être affectés à ce projet
     */
    @Min(value = 1, message = "La capacité doit être au minimum 1")
    @Column(name = "max_students")
    @Builder.Default
    private Integer maxStudents = 1;

    /**
     * Nombre minimum d'étudiants requis pour démarrer le projet
     */
    @Min(value = 1, message = "Le minimum requis doit être au moins 1")
    @Column(name = "min_students")
    @Builder.Default
    private Integer minStudents = 1;

    /**
     * Semestre concerné (1 ou 2)
     */
    @Min(value = 1, message = "Le semestre doit être 1 ou 2")
    @Max(value = 2, message = "Le semestre doit être 1 ou 2")
    @Column(name = "semester")
    private Integer semester;

    /**
     * Année universitaire (exp: 2024-2025)
     */
    @Size(max = 9, message = "L'année universitaire doit être au format YYYY-YYYY")
    @Column(name = "academic_year", length = 9)
    private String academicYear;

    /**
     * Formation cible pour ce projet
     */
    @Size(max = 100, message = "La formation cible ne doit pas dépasser 100 caractères")
    @Column(name = "target_program", length = 100)
    private String targetProgram;

    /**
     * Indique si le projet peut être réalisé en télétravail
     */
    @Column(name = "remote_possible")
    @Builder.Default
    private Boolean remotePossible = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Indique si le projet est complet (plus de places disponibles)
     */
    @Column(name = "full", nullable = false)
    @Builder.Default
    private Boolean full = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enseignant responsable du projet
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Compétences requises comme prérequis
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_required_skills",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @Builder.Default
    private Set<Skill> requiredSkills  = new HashSet<>();

    /**
     * Compétences que le projet permet de développer
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_target_skills",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @Builder.Default
    private Set<Skill> targetSkills = new HashSet<>();

    /**
     * Mots-clés associés au projet
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_keywords",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    @Builder.Default
    private Set<Keyword> keywords = new HashSet<>();

    /**
     * Préférences des étudiants pour ce projet
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentPreference> preferences = new ArrayList<>();

    /**
     * Résultats de matching impliquant ce projet
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MatchingResult> matchingResults = new ArrayList<>();

    /**
     * Étudiants finalement assignés à ce projet
     */
    @OneToMany(mappedBy = "assignedProject")
    @Builder.Default
    private List<Student> assignedStudents = new ArrayList<>();


}
