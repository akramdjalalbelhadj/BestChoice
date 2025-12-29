package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.model.enums.PreferenceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

/**
 * Entité représentant la préférence d'un étudiant pour un projet.
 * Permet à l'étudiant de classer ses choix selon ses priorités.

 */
@Entity
@Table( name = "student_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"student_id", "project_id"}, name = "uk_student_preference_project"),
                @UniqueConstraint(
                        columnNames = {"student_id", "rank"}, name = "uk_student_preference_rank")
        },
        indexes = {
                @Index(name = "idx_preference_student", columnList = "student_id"),
                @Index(name = "idx_preference_project", columnList = "project_id"),
                @Index(name = "idx_preference_rank", columnList = "rank")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StudentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Rang de préférence (1 = premier choix, 2 = deuxième choix ... etc)
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
     * Étudiant ayant exprimé cette préférence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Projet concerné par cette préférence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;


}
