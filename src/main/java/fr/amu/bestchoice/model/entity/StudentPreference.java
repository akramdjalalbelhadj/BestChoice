package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.model.enums.PreferenceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"matching_campaign_id", "student_id", "project_id"},
                        name = "uk_preference_campaign_student_project"
                ),
                @UniqueConstraint(
                        columnNames = {"matching_campaign_id", "student_id", "subject_id"},
                        name = "uk_preference_campaign_student_subject"
                ),
                @UniqueConstraint(
                        columnNames = {"matching_campaign_id", "student_id", "rank"},
                        name = "uk_preference_campaign_student_rank"
                )
        },
        indexes = {
                @Index(name = "idx_preference_campaign", columnList = "matching_campaign_id"),
                @Index(name = "idx_preference_student", columnList = "student_id"),
                @Index(name = "idx_preference_project", columnList = "project_id"),
                @Index(name = "idx_preference_subject", columnList = "subject_id"),
                @Index(name = "idx_preference_rank", columnList = "rank")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Rang de préférence (1 = premier choix, 2 = deuxième choix, ...)
     */
    @Min(value = 1, message = "Le rang doit être au minimum 1")
    @Column(name = "rank", nullable = false)
    private Integer rank;

    /**
     * Motivation ou justification du choix (optionnel)
     */
    @Size(max = 1000, message = "La motivation ne doit pas dépasser 1000 caractères")
    @Column(name = "motivation", length = 1000)
    private String motivation;

    /**
     * Commentaire supplémentaire de l'étudiant
     */
    @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
    @Column(name = "comment", length = 500)
    private String comment;

    /**
     * Statut de la préférence
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PreferenceStatus status = PreferenceStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Campagne de matching concernée
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_campaign_id", nullable = false)
    private MatchingCampaign matchingCampaign;

    /**
     * Étudiant ayant exprimé cette préférence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Projet concerné par cette préférence
     * Utilisé uniquement si la campagne est de type PROJECT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Matière / option concernée par cette préférence
     * Utilisé uniquement si la campagne est de type SUBJECT
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
}