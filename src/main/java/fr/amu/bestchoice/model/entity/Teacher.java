package fr.amu.bestchoice.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "teachers",
        indexes = {
                @Index(name = "idx_teacher_user", columnList = "user_id"),
                @Index(name = "idx_teacher_department", columnList = "department")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teacher_user", columnNames = "user_id")
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 100, message = "Le département ne doit pas dépasser 100 caractères")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * Grade (titre académique)
     * Exp: Maitre de Conférences
     */
    @Size(max = 100, message = "Le grade ne doit pas dépasser 100 caractères")
    @Column(name = "academic_rank", length = 100)
    private String academicRank;

    /**
     * Spécialité ou domaine de recherche
     */
    @Size(max = 200, message = "La spécialité ne doit pas dépasser 200 caractères")
    @Column(name = "specialty", length = 200)
    private String specialty;

    @Column(name = "website_url")
    private String websiteUrl;

    /**
     * Projets proposés par cet enseignant
     */
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

}
