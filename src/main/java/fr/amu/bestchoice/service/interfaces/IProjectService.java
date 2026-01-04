package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des projets.
 */
public interface IProjectService {

    /**
     * Crée un nouveau projet.
     *
     * @param teacherId L'ID de l'enseignant
     * @param dto Les données de création
     * @return Le projet créé
     */
    ProjectResponse create(Long teacherId, ProjectCreateRequest dto);

    /**
     * Met à jour un projet.
     *
     * @param id L'ID du projet
     * @param dto Les données de mise à jour
     * @return Le projet mis à jour
     */
    ProjectResponse update(Long id, ProjectUpdateRequest dto);

    /**
     * Récupère un projet par son ID.
     *
     * @param id L'ID du projet
     * @return Le projet
     */
    ProjectResponse findById(Long id);

    /**
     * Récupère tous les projets avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page de projets
     */
    Page<ProjectResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère tous les projets.
     *
     * @return La liste de tous les projets
     */
    List<ProjectResponse> findAll();

    /**
     * Récupère tous les projets actifs.
     *
     * @return La liste des projets actifs
     */
    List<ProjectResponse> findAllActive();

    /**
     * Récupère tous les projets disponibles (actifs et non complets).
     *
     * @return La liste des projets disponibles
     */
    List<ProjectResponse> findAllAvailable();

    /**
     * Active un projet.
     *
     * @param id L'ID du projet
     */
    void activate(Long id);

    /**
     * Désactive un projet.
     *
     * @param id L'ID du projet
     */
    void deactivate(Long id);
}
