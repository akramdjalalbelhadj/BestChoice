package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.StudentPreference;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des préférences étudiantes
 */
@Repository
public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Long> {

    /**
     * Récupère toutes les préférences d'un étudiant, triées par rang
     */
    @Query("SELECT sp FROM StudentPreference sp WHERE sp.student.id = :studentId ORDER BY sp.rank ASC")
    List<StudentPreference> findByStudentIdOrderByRankAsc(@Param("studentId") Long studentId);

    /**
     * Récupère toutes les préférences pour un projet
     */
    @Query("SELECT sp FROM StudentPreference sp WHERE sp.project.id = :projectId ORDER BY sp.rank ASC")
    List<StudentPreference> findByProjectIdOrderByRankAsc(@Param("projectId") Long projectId);

    /**
     * Récupère une préférence spécifique (étudiant + projet)
     */
    Optional<StudentPreference> findByStudentIdAndProjectId(Long studentId, Long projectId);

    /**
     * Récupère les préférences d'un étudiant par statut
     */
    List<StudentPreference> findByStudentIdAndStatus(Long studentId, PreferenceStatus status);

    /**
     * Vérifie si un étudiant a déjà une préférence pour un projet
     */
    boolean existsByStudentIdAndProjectId(Long studentId, Long projectId);

    /**
     * Vérifie si un rang est déjà utilisé par un étudiant
     */
    boolean existsByStudentIdAndRank(Long studentId, Integer rank);

    /**
     * Récupère le nombre de préférences d'un étudiant
     */
    long countByStudentId(Long studentId);

    /**
     * Récupère le nombre d'étudiants ayant exprimé une préférence pour un projet
     */
    long countByProjectId(Long projectId);

    /**
     * Récupère toutes les préférences en attente
     */
    List<StudentPreference> findByStatus(PreferenceStatus status);

    /**
     * Récupère le rang maximum utilisé par un étudiant
     */
    @Query("SELECT COALESCE(MAX(sp.rank), 0) FROM StudentPreference sp WHERE sp.student.id = :studentId")
    Integer findMaxRankByStudentId(@Param("studentId") Long studentId);

    /**
     * Supprime toutes les préférences d'un étudiant
     */
    void deleteByStudentId(Long studentId);
}
