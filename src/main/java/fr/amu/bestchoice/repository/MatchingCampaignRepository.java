package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingCampaignRepository extends JpaRepository<MatchingCampaign, Long> {

    /**
     * Récupère la campagne avec TOUS ses participants chargés d'un coup.
     * C'est ce qu'on appelle le "Fetch Join" pour éviter le problème du N+1 Select.
     */
    @Query("SELECT DISTINCT c FROM MatchingCampaign c " +
            "LEFT JOIN FETCH c.students " +
            "LEFT JOIN FETCH c.projects " +
            "LEFT JOIN FETCH c.subjects " +
            "WHERE c.id = :id")
    Optional<MatchingCampaign> findWithDetailsById(@Param("id") Long id);

    /**
     *Renvoie une Liste de MatchingCampaign associées à un enseignant donné.
     */
    List<MatchingCampaign> findByTeacherId(Long teacherId);

    @Query("SELECT c FROM MatchingCampaign c JOIN c.students s WHERE s.id = :studentId")
    List<MatchingCampaign> findAllByStudentIdInTable(@Param("studentId") Long studentId);

    // ── Nettoyage des FK avant suppression d'une campagne ────────────────────

    @Modifying
    @Query(value = "DELETE FROM student_preferences WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteStudentPreferencesByCampaignId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM matching_results WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteMatchingResultsByCampaignId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM project_matching_campaigns WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteFromProjectMatchingCampaigns(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM subject_matching_campaigns WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteFromSubjectMatchingCampaigns(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM matching_campaign_students WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteFromMatchingCampaignStudents(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM matching_campaign_projects WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteFromMatchingCampaignProjects(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM matching_campaign_subjects WHERE matching_campaign_id = :id", nativeQuery = true)
    void deleteFromMatchingCampaignSubjects(@Param("id") Long id);
}