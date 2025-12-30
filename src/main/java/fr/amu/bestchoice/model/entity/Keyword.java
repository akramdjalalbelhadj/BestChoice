package fr.amu.bestchoice.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un mot-clé ou centre d'intérêt.
 * Utilisée pour :
 * - Définir les intérêts des étudiants
 * - Taguer les projets avec des mots-clés
 * - Faciliter le matching basé sur les domaines
 */
@Entity
@Table( name = "keywords",
        uniqueConstraints = {@UniqueConstraint(columnNames = "label", name = "uk_keyword_label")},
        indexes = {@Index(name = "idx_keyword_domain", columnList = "domain")}
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le libellé du mot-clé est obligatoire")
    @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
    @Column(name = "label", nullable = false, unique = true, length = 100)
    private String label;

    @Size(max = 300, message = "La description ne doit pas dépasser 300 caractères")
    @Column(name = "description", length = 300)
    private String description;

    /**
     * Domaine auquel appartient ce mot-clé
     * Ex: IA, Web, BDD, Sécurité
     */
    @Size(max = 50, message = "Le domaine ne doit pas dépasser 50 caractères")
    @Column(name = "domain", length = 50)
    private String domain;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Étudiants ayant ce centre d'intérêt
     */
    @ManyToMany(mappedBy = "interests")
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    /**
     * Projets associés à ce mot-clé
     */
    @ManyToMany(mappedBy = "keywords")
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

}
