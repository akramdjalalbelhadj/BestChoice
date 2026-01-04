package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des mots-clés.
 */
public interface IKeywordService {

    /**
     * Crée un nouveau mot-clé.
     *
     * @param dto Les données de création
     * @return Le mot-clé créé
     */
    KeywordResponse create(KeywordCreateRequest dto);

    /**
     * Met à jour un mot-clé.
     *
     * @param id L'ID du mot-clé
     * @param dto Les données de mise à jour
     * @return Le mot-clé mis à jour
     */
    KeywordResponse update(Long id, KeywordUpdateRequest dto);

    /**
     * Récupère un mot-clé par son ID.
     *
     * @param id L'ID du mot-clé
     * @return Le mot-clé
     */
    KeywordResponse findById(Long id);

    /**
     * Récupère tous les mots-clés avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page de mots-clés
     */
    Page<KeywordResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère tous les mots-clés.
     *
     * @return La liste de tous les mots-clés
     */
    List<KeywordResponse> findAll();

    /**
     * Récupère tous les mots-clés actifs.
     *
     * @return La liste des mots-clés actifs
     */
    List<KeywordResponse> findAllActive();

    /**
     * Supprime un mot-clé.
     *
     * @param id L'ID du mot-clé
     */
    void delete(Long id);

    /**
     * Désactive un mot-clé.
     *
     * @param id L'ID du mot-clé
     */
    void deactivate(Long id);
}
