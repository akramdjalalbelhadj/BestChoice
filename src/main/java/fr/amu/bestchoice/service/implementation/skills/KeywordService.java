package fr.amu.bestchoice.service.implementation.referential;

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
import org.springframework.data.domain.Page;              // ⚙️ AJOUT
import org.springframework.data.domain.PageRequest;       // ⚙️ AJOUT
import org.springframework.data.domain.Pageable;          // ⚙️ AJOUT
import org.springframework.data.domain.Sort;              // ⚙️ AJOUT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final KeywordMapper keywordMapper;

    // ==================== CREATE ====================

    @Transactional
    public KeywordResponse create(KeywordCreateRequest dto) {
        log.info("Début création mot-clé : label={}", dto.label());

        // Vérifier que le label n'existe pas déjà
        if (keywordRepository.existsByLabel(dto.label())) {
            log.warn("Tentative de création d'un mot-clé existant : label={}", dto.label());
            throw new BusinessException("Un mot-clé avec le label '" + dto.label() + "' existe déjà");
        }

        Keyword keyword = keywordMapper.toEntity(dto);
        log.debug("Keyword mappé : label={}", keyword.getLabel());

        Keyword savedKeyword = keywordRepository.save(keyword);
        log.info("Mot-clé créé avec succès : id={}, label={}", savedKeyword.getId(), savedKeyword.getLabel());

        return keywordMapper.toResponse(savedKeyword);
    }

    // ==================== UPDATE ====================

    @Transactional
    public KeywordResponse update(Long id, KeywordUpdateRequest dto) {
        log.info("Début mise à jour mot-clé : id={}", id);

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        log.debug("Mot-clé trouvé : label={}", keyword.getLabel());

        // Si le label change, vérifier qu'il n'existe pas déjà
        if (dto.label() != null && !dto.label().equals(keyword.getLabel())) {
            if (keywordRepository.existsByLabel(dto.label())) {
                log.warn("Tentative de modifier vers un label existant : oldLabel={}, newLabel={}",
                        keyword.getLabel(), dto.label());
                throw new BusinessException("Un mot-clé avec le label '" + dto.label() + "' existe déjà");
            }
        }

        keywordMapper.updateEntityFromDto(dto, keyword);
        log.debug("Mot-clé après mise à jour : label={}", keyword.getLabel());

        Keyword updatedKeyword = keywordRepository.save(keyword);
        log.info("Mot-clé mis à jour avec succès : id={}, label={}", updatedKeyword.getId(), updatedKeyword.getLabel());

        return keywordMapper.toResponse(updatedKeyword);
    }

    // ==================== READ ====================

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

    // ⚙️ NOUVELLE MÉTHODE PAGINÉE
    /**
     * ⚙️ Récupère tous les mots-clés avec pagination.
     */
    public Page<KeywordResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération mots-clés paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Keyword> keywordsPage = keywordRepository.findAll(pageable);

        log.info("⚙️ Page de mots-clés récupérée : page={}/{}, total={}",
                keywordsPage.getNumber() + 1, keywordsPage.getTotalPages(), keywordsPage.getTotalElements());

        return keywordsPage.map(keywordMapper::toResponse);
    }

    // ANCIENNE MÉTHODE (rétrocompatibilité)
    public List<KeywordResponse> findAll() {
        log.debug("Récupération de tous les mots-clés");
        List<Keyword> keywords = keywordRepository.findAll();
        log.info("Nombre de mots-clés trouvés : {}", keywords.size());
        return keywordMapper.toResponseList(keywords);
    }

    public List<KeywordResponse> findAllActive() {
        log.debug("Récupération des mots-clés actifs uniquement");
        List<Keyword> keywords = keywordRepository.findByActiveTrue();
        log.info("Nombre de mots-clés actifs trouvés : {}", keywords.size());
        return keywordMapper.toResponseList(keywords);
    }

    // ==================== DELETE ====================

    @Transactional
    public void delete(Long id) {
        log.info("Début suppression mot-clé : id={}", id);

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        keywordRepository.delete(keyword);
        log.info("Mot-clé supprimé avec succès : id={}, label={}", id, keyword.getLabel());
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Transactional
    public void deactivate(Long id) {
        log.info("Début désactivation mot-clé : id={}", id);

        Keyword keyword = keywordRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Mot-clé introuvable : id={}", id);
                    return new NotFoundException("Mot-clé introuvable avec l'ID : " + id);
                });

        if (!keyword.getActive()) {
            log.warn("Mot-clé déjà désactivé : id={}, label={}", id, keyword.getLabel());
            return;
        }

        keyword.setActive(false);
        keywordRepository.save(keyword);
        log.info("Mot-clé désactivé avec succès : id={}, label={}", id, keyword.getLabel());
    }

    // ⚙️ MÉTHODE UTILITAIRE PRIVÉE
    /**
     * ⚙️ Crée un Pageable avec tri.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "label"; // ⚙️ Tri par défaut sur le label pour les mots-clés
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}