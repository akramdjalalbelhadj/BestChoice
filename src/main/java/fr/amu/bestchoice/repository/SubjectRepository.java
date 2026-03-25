package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.model.enums.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des matières optionnelles (subjects).
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("SELECT s FROM Subject s WHERE s.teacher.user.id = :userId")
    List<Subject> findByTeacherId(@Param("userId") Long userId);

    List<Subject> findByActiveTrue();

    List<Subject> findByWorkTypesContaining(WorkType workType);

    @Query("SELECT s FROM Subject s WHERE s.active = true AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Subject> searchActiveSubjects(@Param("query") String query);

    @Query("SELECT s FROM Subject s JOIN s.matchingCampaigns c WHERE c.id = :campaignId")
    List<Subject> findByCampaignId(@Param("campaignId") Long campaignId);

    // ── Méthodes de statistiques (AdminStatsService) ──────────────────────────

    /** Nombre d'options actives */
    long countByActiveTrue();

    /** Capacité totale (somme des maxStudents) */
    @Query("SELECT COALESCE(SUM(s.maxStudents), 0) FROM Subject s")
    Long sumMaxStudents();

    /** Répartition par type de travail : [WorkType, count] */
    @Query("SELECT wt, COUNT(s) FROM Subject s JOIN s.workTypes wt GROUP BY wt")
    List<Object[]> countByWorkType();

    /** Répartition par semestre : [semester, count] */
    @Query("SELECT s.semester, COUNT(s) FROM Subject s " +
           "WHERE s.semester IS NOT NULL GROUP BY s.semester ORDER BY s.semester")
    List<Object[]> countBySemester();

    /** Top enseignants par nombre d'options : [firstName, lastName, count] */
    @Query("SELECT s.teacher.user.firstName, s.teacher.user.lastName, COUNT(s) " +
           "FROM Subject s " +
           "WHERE s.teacher IS NOT NULL AND s.teacher.user IS NOT NULL " +
           "GROUP BY s.teacher.user.id, s.teacher.user.firstName, s.teacher.user.lastName " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> countByTeacherStats();
}