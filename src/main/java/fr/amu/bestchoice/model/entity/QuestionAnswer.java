package fr.amu.bestchoice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant une paire question-réponse pour le chatbot.
 *
 * Cette entité constitue la base de connaissances interne du chatbot,
 * permettant de répondre aux questions fréquentes des étudiants
 * sans avoir recours à une API externe.
 */

@Entity
@Table( name = "chatbot_questions_answers",
        indexes = {
                @Index(name = "idx_qa_category", columnList = "category"),
                @Index(name = "idx_qa_active", columnList = "active")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Question ou formulation type
     */
    @NotBlank(message = "La question est obligatoire")
    @Size(max = 500, message = "La question ne doit pas dépasser 500 caractères")
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    /**
     * Réponse associée à la question
     */
    @NotBlank(message = "La réponse est obligatoire")
    @Size(max = 2000, message = "La réponse ne doit pas dépasser 2000 caractères")
    @Column(name = "answer", nullable = false, length = 2000)
    private String answer;

    /**
     * Catégorie de la question (pour organisation)
     * Ex: Inscription, Projets, Matching, Technique
     */
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Mots-clés associés pour améliorer la recherche
     */
    @Size(max = 500, message = "Les mots-clés ne doivent pas dépasser 500 caractères")
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * Score moyen de satisfaction (1-5) donné par les utilisateurs
     */
    @Column(name = "satisfaction_score")
    private BigDecimal satisfactionScore;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
