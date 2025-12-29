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
     * @param id ID du projet
     * @return Optional contenant le projet avec ses relations
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
     * @return Liste des projets disponibles
     */
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.full = false")
    List<Project> findAvailableProjects();

    /**
     * Récupère les projets par type de travail
     * @param workType Type de travail
     * @return Liste des projets de ce type
     */
    List<Project> findByWorkType(WorkType workType);

    /**
     * Récupère les projets par enseignant
     * @param teacherId ID de l'enseignant
     * @return Liste des projets de cet enseignant
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.id = :teacherId")
    List<Project> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Récupère les projets actifs par enseignant
     * @param teacherId ID de l'enseignant
     * @return Liste des projets actifs de cet enseignant
     */
    @Query("SELECT p FROM Project p WHERE p.teacher.id = :teacherId AND p.active = true")
    List<Project> findActiveProjectsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Récupère les projets par formation cible
     * @param targetProgram Formation cible
     * @return Liste des projets pour cette formation
     */
    List<Project> findByTargetProgram(String targetProgram);

    /**
     * Récupère les projets par semestre et année universitaire
     * @param semester Semestre (1 ou 2)
     * @param academicYear Année universitaire (ex: "2024-2025")
     * @return Liste des projets correspondants
     */
    List<Project> findBySemesterAndAcademicYear(Integer semester, String academicYear);

    /**
     * Récupère les projets possibles en télétravail
     * @return Liste des projets en remote
     */
    List<Project> findByRemotePossibleTrue();

    /**
     * Recherche des projets par titre ou description (insensible à la casse)
     * @param searchTerm Terme de recherche
     * @return Liste des projets correspondants
     */
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Project> searchByTitleOrDescription(@Param("searchTerm") String searchTerm);

    /**
     * Récupère les projets ayant une compétence requise spécifique
     * @param skillId ID de la compétence
     * @return Liste des projets nécessitant cette compétence
     */
    @Query("SELECT p FROM Project p JOIN p.requiredSkills s WHERE s.id = :skillId")
    List<Project> findByRequiredSkillId(@Param("skillId") Long skillId);

    /**
     * Récupère les projets ayant un mot-clé spécifique
     * @param keywordId ID du mot-clé
     * @return Liste des projets avec ce mot-clé
     */
    @Query("SELECT p FROM Project p JOIN p.keywords k WHERE k.id = :keywordId")
    List<Project> findByKeywordId(@Param("keywordId") Long keywordId);

    /**
     * Récupère les projets ayant encore des places disponibles
     * @return Liste des projets non complets
     */
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.full = false " +
           "AND SIZE(p.assignedStudents) < p.maxStudents")
    List<Project> findProjectsWithAvailableSlots();

    /**
     * Compte le nombre d'étudiants assignés à un projet
     * @param projectId ID du projet
     * @return Nombre d'étudiants assignés
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.assignedProject.id = :projectId")
    long countAssignedStudents(@Param("projectId") Long projectId);

    /**
     * Vérifie si un projet a atteint sa capacité maximale
     * @param projectId ID du projet
     * @return true si le projet est complet
     */
    @Query("SELECT CASE WHEN COUNT(s) >= p.maxStudents THEN true ELSE false END " +
           "FROM Project p LEFT JOIN Student s ON s.assignedProject.id = p.id " +
           "WHERE p.id = :projectId GROUP BY p.id, p.maxStudents")
    boolean isProjectFull(@Param("projectId") Long projectId);

    /**
     * Récupère les projets par nombre de crédits
     * @param minCredits Nombre minimum de crédits
     * @param maxCredits Nombre maximum de crédits
     * @return Liste des projets dans cette fourchette
     */
    @Query("SELECT p FROM Project p WHERE p.credits BETWEEN :minCredits AND :maxCredits")
    List<Project> findByCreditsRange(@Param("minCredits") Integer minCredits, 
                                     @Param("maxCredits") Integer maxCredits);
}
