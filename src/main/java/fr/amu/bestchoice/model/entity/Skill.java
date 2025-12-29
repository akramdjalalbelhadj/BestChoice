package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.model.entity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant une compétence technique ou académique
 * Les compétences sont utilisées pour :
 * - Définir le profil des étudiants
 * - Définir les prérequis des projets
 */

@Entity
@Table( name = "skills",
        uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "uk_skill_name")},
        indexes = {@Index(name = "idx_skill_category", columnList = "category")}
)

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la compétence est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Catégorie de la compétence
     * (exp: Programmation, BDD, DevOps ...)
     */
    @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Niveau de difficulté ou d'expertise requis (1-5)
     */
    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Étudiants possédant cette compétence
     */
    @ManyToMany(mappedBy = "skills")
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    /**
     * Projets requérant cette compétence comme prérequis
     */
    @ManyToMany(mappedBy = "requiredSkills")
    @Builder.Default
    private Set<Project> projectsAsRequired = new HashSet<>();

    /**
     * Projets visant à développer cette compétence
     */
    @ManyToMany(mappedBy = "targetSkills")
    @Builder.Default
    private Set<Project> projectsAsTarget = new HashSet<>();

}
