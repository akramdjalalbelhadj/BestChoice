package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.enums.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des étudiants
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Récupère un étudiant avec toutes ses relations (skills, interests, preferences)
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.skills " +
           "LEFT JOIN FETCH s.interests " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithRelations(@Param("id") Long id);

    /**
     * Récupère tous les étudiants avec profil complet
     */
    List<Student> findByProfileCompleteTrue();

    /**
     * Récupère les étudiants par formation
     */
    List<Student> findByProgram(String program);

    /**
     * Récupère les étudiants par année d'étude
     */
    List<Student> findByStudyYear(Integer studyYear);

    /**
     * Récupère les étudiants par parcours
     */
    List<Student> findByTrack(String track);

    /**
     * Récupère les étudiants par type de travail préféré
     */
    List<Student> findByPreferredWorkType(WorkType workType);

    /**
     * Récupère les étudiants possédant une compétence spécifique
     */
    @Query("SELECT s FROM Student s JOIN s.skills sk WHERE sk.id = :skillId")
    List<Student> findBySkillId(@Param("skillId") Long skillId);

    /**
     * Récupère les étudiants ayant un centre d'intérêt spécifique
     */
    @Query("SELECT s FROM Student s JOIN s.interests k WHERE k.id = :keywordId")
    List<Student> findByInterestId(@Param("keywordId") Long keywordId);

    /**
     * Récupère les étudiants non encore assignés à un projet
     */
    @Query("SELECT s FROM Student s WHERE s.assignedProject IS NULL AND s.profileComplete = true")
    List<Student> findUnassignedStudentsWithCompleteProfile();

    /**
     * Récupère les étudiants assignés à un projet spécifique
     */
    @Query("SELECT s FROM Student s WHERE s.assignedProject.id = :projectId")
    List<Student> findByAssignedProjectId(@Param("projectId") Long projectId);

    /**
     * Compte le nombre d'étudiants avec profil complet
     */
    long countByProfileCompleteTrue();

    /**
     * Recherche des étudiants par formation et année
     */
    List<Student> findByProgramAndStudyYear(String program, Integer studyYear);
}
