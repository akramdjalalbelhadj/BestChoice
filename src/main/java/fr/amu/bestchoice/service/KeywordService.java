package fr.amu.bestchoice.service;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.KeywordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des mots-clés (Keywords).
 *
 * Opérations disponibles :
 * - Créer un nouveau mot-clé
 * - Modifier un mot-clé existant
 * - Récupérer un mot-clé par ID
 * - Récupérer tous les mots-clés
 * - Récupérer les mots-clés actifs uniquement
 * - Supprimer un mot-clé
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    // ==================== DÉPENDANCES ====================

    private final KeywordRepository keywordRepository;
    private final KeywordMapper keywordMapper;

    // ==================== CREATE ====================

    /**
     * Crée un nouveau mot-clé.
     *
     * @param dto Les données du nouveau mot-clé
     * @return KeywordResponse avec les données du mot-clé créé
     * @throws BusinessException Si un mot-clé avec le même label existe déjà
     */
    @Transactional
    public KeywordResponse create(KeywordCreateRequest dto) {

        log.info("Début création mot-clé : label={}", dto.label());

        // ===== VALIDATION MÉTIER =====

        // Vérifier qu'un mot-clé avec ce label n'existe pas déjà
        if (keywordRepository.existsByLabel(dto.label())) {
            log.warn("Tentative de création d'un mot-clé existant : label={}", dto.label());
            throw new BusinessException("Un mot-clé avec le label '" + dto.label() + "' existe déjà");
        }

        // ===== MAPPING DTO → ENTITY =====

        Keyword keyword = keywordMapper.toEntity(dto);

        log.debug("Keyword mappé : {}", keyword);

        // ===== SAUVEGARDE =====

        Keyword savedKeyword = keywordRepository.save(keyword);

        log.info("Mot-clé créé avec succès : id={}, label={}", savedKeyword.getId(), savedKeyword.getLabel());

        // ===== MAPPING ENTITY → DTO =====

        return keywordMapper.toResponse(savedKeyword);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un mot-clé existant.
     *
     * @param id L'ID du mot-clé à modifier
     * @param dto Les nouvelles données
     * @return KeywordResponse avec les données mises à jour
     * @throws NotFoundException Si le mot-clé n'existe pas
     * @throws BusinessException Si le nouveau label est déjà utilisé
     */
    @Transactional
    public KeywordResponse update(Long id, KeywordUpdateRequest dto) {

        log.info("Début mise à jour mot-clé : id={}", id);

        // ===== RÉCUPÉRATION =====

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        log.debug("Mot-clé trouvé : label={}", keyword.getLabel());

        // ===== VALIDATION MÉTIER =====

        // Si le label change, vérifier qu'il n'est pas déjà utilisé
        if (dto.label() != null && !dto.label().equals(keyword.getLabel())) {
            if (keywordRepository.existsByLabel(dto.label())) {
                log.warn("Tentative de renommer vers un label existant : oldLabel={}, newLabel={}",
                        keyword.getLabel(), dto.label());
                throw new BusinessException("Un mot-clé avec le label '" + dto.label() + "' existe déjà");
            }
        }

        // ===== MAPPING DTO → ENTITY =====

        keywordMapper.updateEntityFromDto(dto, keyword);

        log.debug("Mot-clé après mise à jour : {}", keyword);

        // ===== SAUVEGARDE =====

        Keyword updatedKeyword = keywordRepository.save(keyword);

        log.info("Mot-clé mis à jour avec succès : id={}, label={}", updatedKeyword.getId(), updatedKeyword.getLabel());

        // ===== MAPPING ENTITY → DTO =====

        return keywordMapper.toResponse(updatedKeyword);
    }

    // ==================== READ ====================

    /**
     * Récupère un mot-clé par son ID.
     *
     * @param id L'ID du mot-clé
     * @return KeywordResponse
     * @throws NotFoundException Si le mot-clé n'existe pas
     */
    public KeywordResponse findById(Long id) {

        log.debug("Recherche mot-clé par ID : id={}", id);

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        log.debug("Mot-clé trouvé : label={}", keyword.getLabel());

        return keywordMapper.toResponse(keyword);
    }

    /**
     * Récupère tous les mots-clés.
     *
     * @return Liste de KeywordResponse
     */
    public List<KeywordResponse> findAll() {

        log.debug("Récupération de tous les mots-clés");

        List<Keyword> keywords = keywordRepository.findAll();

        log.info("Nombre de mots-clés trouvés : {}", keywords.size());

        return keywordMapper.toResponseList(keywords);
    }

    /**
     * Récupère uniquement les mots-clés actifs.
     *
     * @return Liste de KeywordResponse
     */
    public List<KeywordResponse> findAllActive() {

        log.debug("Récupération des mots-clés actifs uniquement");

        List<Keyword> keywords = keywordRepository.findByActiveTrue();

        log.info("Nombre de mots-clés actifs trouvés : {}", keywords.size());

        return keywordMapper.toResponseList(keywords);
    }

    // ==================== DELETE ====================

    /**
     * Supprime un mot-clé.
     *
     * @param id L'ID du mot-clé à supprimer
     * @throws NotFoundException Si le mot-clé n'existe pas
     */
    @Transactional
    public void delete(Long id) {

        log.info("Début suppression mot-clé : id={}", id);

        if (!keywordRepository.existsById(id)) {
            log.error("Tentative de suppression d'un mot-clé inexistant : id={}", id);
            throw new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
        }

        keywordRepository.deleteById(id);

        log.info("Mot-clé supprimé avec succès : id={}", id);
    }

    /**
     * Désactive un mot-clé (soft delete).
     *
     * @param id L'ID du mot-clé à désactiver
     * @throws NotFoundException Si le mot-clé n'existe pas
     */
    @Transactional
    public void deactivate(Long id) {

        log.info("Début désactivation mot-clé : id={}", id);

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        keyword.setActive(false);
        keywordRepository.save(keyword);

        log.info("Mot-clé désactivé avec succès : id={}, label={}", id, keyword.getLabel());
    }
}