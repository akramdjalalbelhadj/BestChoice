package fr.amu.bestchoice.model.entity;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table( name = "students",
        indexes = {
                @Index(name = "idx_student_program", columnList = "program"),
                @Index(name = "idx_student_year", columnList = "study_year")})


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Student{

    @Id
    private Long id;

    //Relation 1 : 1 avec User (meme ID)
    @OneToOne(fetch = FetchType.LAZY)
    //@MapsId signifie que L'id de Teacher est l'id de User (ID partagé)
    @MapsId
    // La colonne id est à la fois :PK de teacher et FK vers users
    @JoinColumn(name = "id")
    private User user;

    /**
     * Formation actuelle de l'étudiant
     * Exp: Master Informatique
     */
    @Size(max = 100, message = "La formation ne doit pas dépasser 100 caractères")
    @Column(name = "program", length = 100)
    private String program;

    /**
     * Année d'étude (1 --> 8 pour couvrir License1 à Doctorat)
     */
    @Min(value = 1, message = "L'année d'étude doit être au minimum 1")
    @Max(value = 8, message = "L'année d'étude doit être au maximum 8")
    @Column(name = "study_year")
    private Integer studyYear;

    /**
     * Parcours ou spécialisation
     * Exp : IAAA, IDL,SID, GIG
     */
    @Size(max = 100, message = "Le parcours ne doit pas dépasser 100 caractères")
    @Column(name = "track", length = 100)
    private String track;

    /**
     * Département ou UFR
     * exep: UFR Sciences
     */
    @Size(max = 100, message = "Le département ne doit pas dépasser 100 caractères")
    @Column(name = "department", length = 100)
    private String department;

    /**
     * Type de travail préféré par l'étudiant
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_work_type", length = 30)
    private WorkType preferredWorkType;

    /**
     * Domaine professionnel visé par l'étudiant
     */
    @Size(max = 200, message = "Le domaine visé ne doit pas dépasser 200 caractères")
    @Column(name = "target_field", length = 200)
    private String targetField;

    /**
     * Biographie ou présentation de l'étudiant
     */
    @Size(max = 1000, message = "La biographie ne doit pas dépasser 1000 caractères")
    @Column(name = "bio", length = 1000)
    private String bio;


    @Column(name = "cv_url")
    private String cvUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    /**
     * Indique si le profil est complet et prêt pour le matching
     */
    @Column(name = "profile_complete", nullable = false)
    private Boolean profileComplete = false;

    /**
     * Compétences de l'étudiant
     * Un étudiant peut avoir plusieurs(0:n) compétences
     * et une compétence peut appartenir à plusieurs étudiants
     *
     * pourquoi inverseJoin ?
     * --> inverseJoinColumns : Colonne FK vers l'entité inverse (Skill)
     * Exp :
     * Akram connait : Java, PYTHON, SQL
     * Yacine connait : Java, REACT, ANGULAR
     * Java est connu par : Akram, Yacine
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    /**
     * Centres d'intérêt de l'étudiant (mots-clés)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_interests",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id"))
    @Builder.Default
    private Set<Keyword> interests = new HashSet<>();

    /**
     * Préférences de l'étudiant pour les projets (classement)
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rank ASC")
    @Builder.Default
    private List<StudentPreference> preferences = new ArrayList<>();

    /**
     * Résultats de matching pour cet étudiant
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("globalScore DESC")
    @Builder.Default
    private List<MatchingResult> matchingResults = new ArrayList<>();

    /**
     * Projet finalement assigné à l'étudiant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_project_id")
    private Project assignedProject;

}
