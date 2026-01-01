package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByStudentNumber(String studentNumber);
    boolean existsByStudentNumber(String studentNumber);
    /**
     * Recherche un utilisateur par email avec ses rôles chargés
     * Évite le N+1 problem grâce à JOIN FETCH
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Récupère tous les utilisateurs actifs
     */
    List<User> findByActiveTrue();

    /**
     * Récupère tous les utilisateurs ayant un rôle spécifique
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    /**
     * Recherche des utilisateurs par nom ou prénom (insensible à la casse)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Compte le nombre d'utilisateurs actifs
     */
    long countByActiveTrue();
}
