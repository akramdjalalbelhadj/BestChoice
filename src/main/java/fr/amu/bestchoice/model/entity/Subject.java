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
@Table(
        name = "subjects",
        indexes = {
                @Index(name = "idx_subject_teacher", columnList = "teacher_id"),
                @Index(name = "idx_subject_work_type", columnList = "work_type"),
                @Index(name = "idx_subject_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom de la matière / option
     */
    @NotBlank(message = "Le titre de la matière est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Description de la matière / option
     */
    @NotBlank(message = "La description de la matière est obligatoire")
    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    @Column(name = "description", nullable = false, length = 5000)
    private String description;

    /**
     * Objectifs pédagogiques
     */
    @Size(max = 2000, message = "Les objectifs ne doivent pas dépasser 2000 caractères")
    @Column(name = "objectives", length = 2000)
    private String objectives;

    /**
     * Type de travail dominant de la matière
     * Ex: RESEARCH, DEVELOPMENT, THEORETICAL, PRACTICAL...
     */
    @ElementCollection(targetClass = WorkType.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "subject_work_types", joinColumns = @JoinColumn(name = "subject_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "work_type")
    @NotEmpty(message = "Au moins un type de travail est obligatoire")
    @Builder.Default
    private Set<WorkType> workTypes = new HashSet<>();

    /**
     * Capacité maximale d'étudiants
     */
    @Min(value = 1, message = "La capacité doit être au minimum 1")
    @Column(name = "max_students")
    @Builder.Default
    private Integer maxStudents = 1;

    /**
     * Nombre minimum d'étudiants
     */
    @Min(value = 1, message = "Le minimum requis doit être au moins 1")
    @Column(name = "min_students")
    @Builder.Default
    private Integer minStudents = 1;

    /**
     * Nombre de crédits ECTS
     */
    @Min(value = 1, message = "Le nombre de crédits doit être au minimum 1")
    @Max(value = 30, message = "Le nombre de crédits ne peut pas dépasser 30")
    @Column(name = "credits")
    private Integer credits;

    /**
     * Semestre concerné
     */
    @Min(value = 1, message = "Le semestre doit être 1 ou 2")
    @Max(value = 2, message = "Le semestre doit être 1 ou 2")
    @Column(name = "semester")
    private Integer semester;

    /**
     * Année universitaire
     */
    @Size(max = 9, message = "L'année universitaire doit être au format YYYY-YYYY")
    @Column(name = "academic_year", length = 9)
    private String academicYear;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enseignant responsable
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Compétences requises
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_required_skills",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> requiredSkills = new HashSet<>();

    /**
     * Compétences développées
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_target_skills",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> targetSkills = new HashSet<>();

    /**
     * Mots-clés associés
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_keywords",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private Set<Keyword> keywords = new HashSet<>();

    /**
     * Préférences des étudiants pour cette matière
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentPreference> preferences = new ArrayList<>();
}