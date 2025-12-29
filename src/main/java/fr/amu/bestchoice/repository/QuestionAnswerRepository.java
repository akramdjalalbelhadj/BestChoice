package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des questions-réponses du chatbot
 */
@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {

    /**
     * Récupère toutes les Q&R actives
     */
    List<QuestionAnswer> findByActiveTrue();

    /**
     * Récupère les Q&R par catégorie
     */
    List<QuestionAnswer> findByCategory(String category);

    /**
     * Récupère les Q&R actives d'une catégorie
     */
    List<QuestionAnswer> findByCategoryAndActiveTrue(String category);

    /**
     * Recherche dans les questions (insensible à la casse)
     */
    @Query("SELECT qa FROM QuestionAnswer qa WHERE qa.active = true " +
           "AND LOWER(qa.question) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<QuestionAnswer> searchByQuestion(@Param("searchTerm") String searchTerm);

    /**
     * Recherche dans les tags
     */
    @Query("SELECT qa FROM QuestionAnswer qa WHERE qa.active = true " +
           "AND LOWER(qa.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<QuestionAnswer> findByTag(@Param("tag") String tag);

    /**
     * Recherche globale dans questions, réponses et tags
     */
    @Query("SELECT qa FROM QuestionAnswer qa WHERE qa.active = true " +
           "AND (LOWER(qa.question) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(qa.answer) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(qa.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<QuestionAnswer> searchGlobal(@Param("searchTerm") String searchTerm);

    /**
     * Récupère les Q&R les mieux notées
     */
    @Query("SELECT qa FROM QuestionAnswer qa WHERE qa.active = true " +
           "AND qa.satisfactionScore IS NOT NULL " +
           "ORDER BY qa.satisfactionScore DESC")
    List<QuestionAnswer> findTopRated(org.springframework.data.domain.Pageable pageable);

    /**
     * Récupère toutes les catégories distinctes
     */
    @Query("SELECT DISTINCT qa.category FROM QuestionAnswer qa " +
           "WHERE qa.category IS NOT NULL ORDER BY qa.category")
    List<String> findAllCategories();

    /**
     * Compte le nombre de Q&R par catégorie
     */
    long countByCategory(String category);
}
