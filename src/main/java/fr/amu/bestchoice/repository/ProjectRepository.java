package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.enums.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des projets
 */

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Récupère un projet avec toutes ses relations chargées
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN FETCH p.requiredSkills " +
           "LEFT JOIN FETCH p.targetSkills " +
           "LEFT JOIN FETCH p.keywords " +
           "WHERE p.id = :id")
    Optional<Project> findByIdWithRelations(@Param("id") Long id);

    /**
     * Récupère tous les projets actifs
     */
    List<Project> findByActiveTrue();

    /**
     * Récupère les projets actifs et non complets
     */
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.complet = false")
    List<Project> findAvailableProjects();

    /**
     * Récupère les projets par type de travail
     * @param workType Type de travail
     * @return Liste des projets de ce type
     */
    List<Project> findByWorkTypesContaining(WorkType workType);
    /**
     * Récupère les projets par enseignant (recherche par user.id)
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.user.id = :userId")
    List<Project> findByTeacherId(@Param("userId") Long userId);

    /**
     * Récupère les projets actifs par enseignant (recherche par user.id)
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.user.id = :userId AND p.active = true")
    List<Project> findActiveProjectsByTeacherId(@Param("userId") Long userId);

    /**
     * Récupère les projets par formation cible
     */
    List<Project> findByTargetProgram(String targetProgram);

    /**
     * Récupère les projets par semestre et année universitaire
     */
    List<Project> findBySemesterAndAcademicYear(Integer semester, String academicYear);

    /**
     * Récupère les projets possibles en télétravail
     */
    List<Project> findByRemotePossibleTrue();

    /**
     * Recherche des projets par titre ou description (insensible à la casse)
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Project> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Récupère les projets ayant une compétence requise spécifique
     */
    @Query("SELECT p FROM Project p JOIN p.requiredSkills s WHERE s.id = :skillId")
    List<Project> findByRequiredSkillId(@Param("skillId") Long skillId);

    /**
     * Récupère les projets ayant un mot-clé spécifique
     */
    @Query("SELECT p FROM Project p JOIN p.keywords k WHERE k.id = :keywordId")
    List<Project> findByKeywordId(@Param("keywordId") Long keywordId);

    /**
     * Récupère les projets ayant encore des places disponibles
     */
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.complet = false " +
           "AND SIZE(p.assignedStudents) < p.maxStudents")
    List<Project> findProjectsWithAvailableSlots();

    /**
     * Compte le nombre d'étudiants assignés à un projet
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.assignedProject.id = :projectId")
    long countAssignedStudents(@Param("projectId") Long projectId);

    /**
     * Vérifie si un projet a atteint sa capacité maximale
     */
    @Query("SELECT CASE WHEN COUNT(s) >= p.maxStudents THEN true ELSE false END " +
           "FROM Project p LEFT JOIN Student s ON s.assignedProject.id = p.id " +
           "WHERE p.id = :projectId GROUP BY p.id, p.maxStudents")
    boolean isProjectComplet(@Param("projectId") Long projectId);

    /**
     * Récupère les projets par nombre de crédits
     */
    @Query("SELECT p FROM Project p WHERE p.credits BETWEEN :minCredits AND :maxCredits")
    List<Project> findByCreditsRange(@Param("minCredits") Integer minCredits, 
                                     @Param("maxCredits") Integer maxCredits);

    List<Project> findAllByActiveTrue();

    /**
     * Récupère les projets associés à une campagne spécifique
     */
    @Query("SELECT p FROM Project p JOIN p.matchingCampaigns c WHERE c.id = :campaignId")
    List<Project> findByCampaignId(@Param("campaignId") Long campaignId);

    // ── Méthodes de statistiques (AdminStatsService) ──────────────────────────

    /** Nombre de projets actifs */
    long countByActiveTrue();

    /** Nombre de projets complets */
    long countByCompletTrue();

    /** Capacité totale (somme des maxStudents) */
    @Query("SELECT COALESCE(SUM(p.maxStudents), 0) FROM Project p")
    Long sumMaxStudents();

    /** Répartition par type de travail : [WorkType, count] */
    @Query("SELECT wt, COUNT(p) FROM Project p JOIN p.workTypes wt GROUP BY wt")
    List<Object[]> countByWorkType();

    /** Répartition par semestre : [semester, count] */
    @Query("SELECT p.semester, COUNT(p) FROM Project p " +
           "WHERE p.semester IS NOT NULL GROUP BY p.semester ORDER BY p.semester")
    List<Object[]> countBySemester();

    /** Top enseignants par nombre de projets : [firstName, lastName, count] */
    @Query("SELECT p.teacher.user.firstName, p.teacher.user.lastName, COUNT(p) " +
           "FROM Project p " +
           "WHERE p.teacher IS NOT NULL AND p.teacher.user IS NOT NULL " +
           "GROUP BY p.teacher.user.id, p.teacher.user.firstName, p.teacher.user.lastName " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countByTeacherStats();
}
