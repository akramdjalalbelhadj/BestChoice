package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des compétences
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByName(String name);
    boolean existsByName(String name);
    
    List<Skill> findByActiveTrue();
    
    List<Skill> findByCategory(String category);
    
    @Query("SELECT DISTINCT s.category FROM Skill s WHERE s.category IS NOT NULL ORDER BY s.category")
    List<String> findAllCategories();
    
    List<Skill> findByLevel(Integer level);
    
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Skill> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
}
