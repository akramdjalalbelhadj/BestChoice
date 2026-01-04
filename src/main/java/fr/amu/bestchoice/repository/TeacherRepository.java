package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des enseignants
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    /**
     * Récupère un enseignant avec tous ses projets
     */
    @Query("SELECT t FROM Teacher t LEFT JOIN FETCH t.projects WHERE t.id = :id")
    Optional<Teacher> findByIdWithProjects(@Param("id") Long id);

    /**
     * Récupère les enseignants par département
     */
    List<Teacher> findByDepartment(String department);

    /**
     * Récupère les enseignants par grade académique
     */
    List<Teacher> findByAcademicRank(String academicRank);

    /**
     * Récupère les enseignants par spécialité
     */
    @Query("SELECT t FROM Teacher t WHERE LOWER(t.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Teacher> findBySpecialtyContaining(@Param("specialty") String specialty);

    /**
     * Récupère les enseignants ayant au moins un projet actif
     */
    @Query("SELECT DISTINCT t FROM Teacher t JOIN t.projects p WHERE p.active = true")
    List<Teacher> findTeachersWithActiveProjects();

    /**
     * Compte le nombre de projets d'un enseignant
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.teacher.id = :teacherId")
    long countProjectsByTeacherId(@Param("teacherId") Long teacherId);
}
