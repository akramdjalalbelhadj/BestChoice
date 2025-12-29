package fr.amu.bestchoice.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table( name = "teachers",
        indexes = {
                @Index(name = "idx_teacher_department", columnList = "department")})


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher{

    @Id
    private Long id;

    //Relation 1 : 1 avec User (meme ID)
    @OneToOne(fetch = FetchType.LAZY)
    //@MapsId signifie que L'id de Teacher est l'id de User (ID partagé)
    @MapsId
    // La colonne id est à la fois :PK de teacher et FK vers users
    @JoinColumn(name = "id")
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
