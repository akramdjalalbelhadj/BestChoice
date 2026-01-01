package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des compétences.
 */
public interface ISkillService {

    /**
     * Crée une nouvelle compétence.
     *
     * @param dto Les données de création
     * @return La compétence créée
     */
    SkillResponse create(SkillCreateRequest dto);

    /**
     * Met à jour une compétence.
     *
     * @param id L'ID de la compétence
     * @param dto Les données de mise à jour
     * @return La compétence mise à jour
     */
    SkillResponse update(Long id, SkillUpdateRequest dto);

    /**
     * Récupère une compétence par son ID.
     *
     * @param id L'ID de la compétence
     * @return La compétence
     */
    SkillResponse findById(Long id);

    /**
     * Récupère toutes les compétences avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page de compétences
     */
    Page<SkillResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère toutes les compétences.
     *
     * @return La liste de toutes les compétences
     */
    List<SkillResponse> findAll();

    /**
     * Récupère toutes les compétences actives.
     *
     * @return La liste des compétences actives
     */
    List<SkillResponse> findAllActive();

    /**
     * Supprime une compétence.
     *
     * @param id L'ID de la compétence
     */
    void delete(Long id);

    /**
     * Désactive une compétence.
     *
     * @param id L'ID de la compétence
     */
    void deactivate(Long id);
}
