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
     * @param id ID de l'étudiant
     * @return Optional contenant l'étudiant avec ses relations
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.skills " +
           "LEFT JOIN FETCH s.interests " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithRelations(@Param("id") Long id);

    /**
     * Récupère tous les étudiants avec profil complet
     * @return Liste des étudiants avec profil complet
     */
    List<Student> findByProfileCompleteTrue();

    /**
     * Récupère les étudiants par formation
     * @param program Nom de la formation
     * @return Liste des étudiants de cette formation
     */
    List<Student> findByProgram(String program);

    /**
     * Récupère les étudiants par année d'étude
     * @param studyYear Année d'étude
     * @return Liste des étudiants de cette année
     */
    List<Student> findByStudyYear(Integer studyYear);

    /**
     * Récupère les étudiants par parcours
     * @param track Nom du parcours (ex: IDL, IAAA)
     * @return Liste des étudiants de ce parcours
     */
    List<Student> findByTrack(String track);

    /**
     * Récupère les étudiants par type de travail préféré
     * @param workType Type de travail préféré
     * @return Liste des étudiants avec cette préférence
     */
    List<Student> findByPreferredWorkType(WorkType workType);

    /**
     * Récupère les étudiants possédant une compétence spécifique
     * @param skillId ID de la compétence
     * @return Liste des étudiants possédant cette compétence
     */
    @Query("SELECT s FROM Student s JOIN s.skills sk WHERE sk.id = :skillId")
    List<Student> findBySkillId(@Param("skillId") Long skillId);

    /**
     * Récupère les étudiants ayant un centre d'intérêt spécifique
     * @param keywordId ID du mot-clé
     * @return Liste des étudiants ayant cet intérêt
     */
    @Query("SELECT s FROM Student s JOIN s.interests k WHERE k.id = :keywordId")
    List<Student> findByInterestId(@Param("keywordId") Long keywordId);

    /**
     * Récupère les étudiants non encore assignés à un projet
     * @return Liste des étudiants sans projet assigné
     */
    @Query("SELECT s FROM Student s WHERE s.assignedProject IS NULL AND s.profileComplete = true")
    List<Student> findUnassignedStudentsWithCompleteProfile();

    /**
     * Récupère les étudiants assignés à un projet spécifique
     * @param projectId ID du projet
     * @return Liste des étudiants assignés à ce projet
     */
    @Query("SELECT s FROM Student s WHERE s.assignedProject.id = :projectId")
    List<Student> findByAssignedProjectId(@Param("projectId") Long projectId);

    /**
     * Compte le nombre d'étudiants avec profil complet
     * @return Nombre d'étudiants avec profil complet
     */
    long countByProfileCompleteTrue();

    /**
     * Recherche des étudiants par formation et année
     * @param program Formation
     * @param studyYear Année d'étude
     * @return Liste des étudiants correspondants
     */
    List<Student> findByProgramAndStudyYear(String program, Integer studyYear);
}
