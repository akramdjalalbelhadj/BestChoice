package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // ✅ On utilise List
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
}