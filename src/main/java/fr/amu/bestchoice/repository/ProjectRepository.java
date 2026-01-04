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
    List<Project> findByWorkType(WorkType workType);

    /**
     * Récupère les projets par enseignant
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.id = :teacherId")
    List<Project> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Récupère les projets actifs par enseignant
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.id = :teacherId AND p.active = true")
    List<Project> findActiveProjectsByTeacherId(@Param("teacherId") Long teacherId);

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

}
