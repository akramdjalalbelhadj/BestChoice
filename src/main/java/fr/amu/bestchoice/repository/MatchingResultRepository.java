package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.MatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des résultats de matching
 */
@Repository
public interface MatchingResultRepository extends JpaRepository<MatchingResult, Long> {

    /**
     * Récupère tous les résultats pour un étudiant, triés par score décroissant
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "ORDER BY mr.globalScore DESC")
    List<MatchingResult> findByStudentIdOrderByGlobalScoreDesc(@Param("studentId") Long studentId);

    /**
     * Récupère tous les résultats pour un projet
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.project.id = :projectId " +
           "ORDER BY mr.globalScore DESC")
    List<MatchingResult> findByProjectIdOrderByGlobalScoreDesc(@Param("projectId") Long projectId);

    /**
     * Récupère les résultats d'une session de matching spécifique pour un étudiant
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "AND mr.sessionId = :sessionId ORDER BY mr.globalScore DESC")
    List<MatchingResult> findByStudentIdAndSessionIdOrderByGlobalScoreDesc(
            @Param("studentId") Long studentId, 
            @Param("sessionId") String sessionId);

    /**
     * Récupère tous les résultats d'une session de matching
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.sessionId = :sessionId " +
           "ORDER BY mr.student.id, mr.globalScore DESC")
    List<MatchingResult> findBySessionIdOrderByStudentAndScore(@Param("sessionId") String sessionId);

    /**
     * Récupère les résultats au-dessus du seuil pour un étudiant
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "AND mr.aboveThreshold = true ORDER BY mr.globalScore DESC")
    List<MatchingResult> findByStudentIdAndAboveThresholdTrue(@Param("studentId") Long studentId);

    /**
     * Récupère un résultat spécifique (étudiant + projet + session)
     */
    Optional<MatchingResult> findByStudentIdAndProjectIdAndSessionId(
            Long studentId, Long projectId, String sessionId);

    /**
     * Récupère les N meilleurs résultats pour un étudiant
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "ORDER BY mr.globalScore DESC")
    List<MatchingResult> findTopNByStudentId(@Param("studentId") Long studentId, 
                                             org.springframework.data.domain.Pageable pageable);

    /**
     * Récupère les résultats avec un score minimum
     */
    @Query("SELECT mr FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "AND mr.globalScore >= :minScore ORDER BY mr.globalScore DESC")
    List<MatchingResult> findByStudentIdAndMinScore(@Param("studentId") Long studentId, 
                                                     @Param("minScore") BigDecimal minScore);

    /**
     * Compte le nombre de résultats pour un étudiant
     */
    long countByStudentId(Long studentId);

    /**
     * Compte le nombre de résultats pour une session
     */
    long countBySessionId(String sessionId);

    /**
     * Supprime tous les résultats d'un étudiant
     */
    @Modifying
    @Query("DELETE FROM MatchingResult mr WHERE mr.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);

    /**
     * Supprime tous les résultats d'une session
     */
    @Modifying
    @Query("DELETE FROM MatchingResult mr WHERE mr.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") String sessionId);

    /**
     * Supprime les anciens résultats (hors session actuelle)
     */
    @Modifying
    @Query("DELETE FROM MatchingResult mr WHERE mr.student.id = :studentId " +
           "AND mr.sessionId != :currentSessionId")
    void deleteOldResultsByStudentId(@Param("studentId") Long studentId, 
                                     @Param("currentSessionId") String currentSessionId);

    /**
     * Récupère les statistiques de matching pour un étudiant
     */
    @Query("SELECT AVG(mr.globalScore) FROM MatchingResult mr WHERE mr.student.id = :studentId")
    BigDecimal calculateAverageScoreByStudentId(@Param("studentId") Long studentId);

    /**
     * Vérifie si un étudiant a des résultats de matching
     */
    boolean existsByStudentId(Long studentId);
}
