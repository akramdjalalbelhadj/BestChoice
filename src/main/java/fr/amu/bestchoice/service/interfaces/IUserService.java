package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des utilisateurs.
 */
public interface IUserService {

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param dto Les données d'inscription
     * @return Les informations de l'utilisateur inscrit
     */
    RegisterResponse register(RegisterRequest dto);

    /**
     * Met à jour un utilisateur.
     *
     * @param id L'ID de l'utilisateur
     * @param dto Les données de mise à jour
     * @return L'utilisateur mis à jour
     */
    UserResponse update(Long id, UserUpdateRequest dto);

    /**
     * Récupère un utilisateur par son ID.
     *
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur
     */
    UserResponse findById(Long id);

    /**
     * Récupère tous les utilisateurs avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page d'utilisateurs
     */
    Page<UserResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère tous les utilisateurs.
     *
     * @return La liste de tous les utilisateurs
     */
    List<UserResponse> findAll();

    /**
     * Récupère tous les utilisateurs actifs.
     *
     * @return La liste des utilisateurs actifs
     */
    List<UserResponse> findAllActive();

    /**
     * Désactive un utilisateur.
     *
     * @param id L'ID de l'utilisateur
     */
    void deactivate(Long id);

    /**
     * Active un utilisateur.
     *
     * @param id L'ID de l'utilisateur
     */
    void activate(Long id);
}
