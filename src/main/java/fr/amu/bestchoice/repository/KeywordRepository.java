package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des mots-clés
 */
@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    Optional<Keyword> findByLabel(String label);
    
    boolean existsByLabel(String label);
    
    List<Keyword> findByActiveTrue();
    
    List<Keyword> findByDomain(String domain);
    
    @Query("SELECT DISTINCT k.domain FROM Keyword k WHERE k.domain IS NOT NULL ORDER BY k.domain")
    List<String> findAllDomains();
    
    @Query("SELECT k FROM Keyword k WHERE LOWER(k.label) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(k.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Keyword> searchByLabelOrDescription(@Param("searchTerm") String searchTerm);
}
