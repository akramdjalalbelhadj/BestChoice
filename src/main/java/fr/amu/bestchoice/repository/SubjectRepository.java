package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des matières optionnelles (subjects).
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Récupère toutes les matières d'un enseignant spécifique.
     * Utile pour afficher les 10 matières de Jean, par exemple.
     */
    List<Subject> findByTeacherId(Long teacherId);

    /**
     * Récupère uniquement les matières actives.
     */
    List<Subject> findByActiveTrue();

    /**
     * Recherche des matières par titre (insensible à la casse).
     */
    List<Subject> findByTitleContainingIgnoreCase(String title);
}