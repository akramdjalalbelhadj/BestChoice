package fr.amu.bestchoice.service.implementation.skills;

import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.service.interfaces.ISkillService;
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SkillMapper;
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
public class SkillService implements ISkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    // ==================== CREATE ====================

    @Transactional
    public SkillResponse create(SkillCreateRequest dto) {
        // Normalisation : trim + première lettre majuscule, reste inchangé
        String normalizedName = normalize(dto.name());
        log.info("Début création compétence : name={}", normalizedName);

        // Vérifier que le nom n'existe pas déjà (insensible à la casse)
        if (skillRepository.existsByNameIgnoreCase(normalizedName)) {
            log.warn("Tentative de création d'une compétence existante : name={}", normalizedName);
            throw new BusinessException("Une compétence avec le nom '" + normalizedName + "' existe déjà");
        }

        Skill skill = skillMapper.toEntity(dto);
        skill.setName(normalizedName);
        log.debug("Skill mappé : name={}", skill.getName());

        Skill savedSkill = skillRepository.save(skill);
        log.info("Compétence créée avec succès : id={}, name={}", savedSkill.getId(), savedSkill.getName());

        return skillMapper.toResponse(savedSkill);
    }

    // ==================== UPDATE ====================

    @Transactional
    public SkillResponse update(Long id, SkillUpdateRequest dto) {
        log.info("Début mise à jour compétence : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        log.debug("Compétence trouvée : name={}", skill.getName());

        // Si le nom change, normaliser et vérifier qu'il n'existe pas déjà (insensible à la casse)
        if (dto.name() != null && !dto.name().equalsIgnoreCase(skill.getName())) {
            String normalizedName = normalize(dto.name());
            if (skillRepository.existsByNameIgnoreCase(normalizedName)) {
                log.warn("Tentative de modifier vers un nom existant : oldName={}, newName={}",
                        skill.getName(), normalizedName);
                throw new BusinessException("Une compétence avec le nom '" + normalizedName + "' existe déjà");
            }
        }

        skillMapper.updateEntityFromDto(dto, skill);
        // Appliquer la normalisation après mapping
        if (skill.getName() != null) skill.setName(normalize(skill.getName()));
        log.debug("Compétence après mise à jour : name={}", skill.getName());

        Skill updatedSkill = skillRepository.save(skill);
        log.info("Compétence mise à jour avec succès : id={}, name={}", updatedSkill.getId(), updatedSkill.getName());

        return skillMapper.toResponse(updatedSkill);
    }

    // ==================== READ ====================

    public SkillResponse findById(Long id) {
        log.debug("Recherche compétence par ID : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        log.debug("Compétence trouvée : name={}", skill.getName());
        return skillMapper.toResponse(skill);
    }

    // ⚙️ NOUVELLE MÉTHODE PAGINÉE
    /**
     * ⚙️ Récupère toutes les compétences avec pagination.
     */
    public Page<SkillResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération compétences paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Skill> skillsPage = skillRepository.findAll(pageable);

        log.info("⚙️ Page de compétences récupérée : page={}/{}, total={}",
                skillsPage.getNumber() + 1, skillsPage.getTotalPages(), skillsPage.getTotalElements());

        return skillsPage.map(skillMapper::toResponse);
    }

    // ANCIENNE MÉTHODE (rétrocompatibilité)
    public List<SkillResponse> findAll() {
        log.debug("Récupération de toutes les compétences");
        List<Skill> skills = skillRepository.findAll();
        log.info("Nombre de compétences trouvées : {}", skills.size());
        return skillMapper.toResponseList(skills);
    }

    public List<SkillResponse> findAllActive() {
        log.debug("Récupération des compétences actives uniquement");
        List<Skill> skills = skillRepository.findByActiveTrue();
        log.info("Nombre de compétences actives trouvées : {}", skills.size());
        return skillMapper.toResponseList(skills);
    }

    // ==================== DELETE ====================

    @Transactional
    public void delete(Long id) {
        log.info("Début suppression compétence : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        skillRepository.delete(skill);
        log.info("Compétence supprimée avec succès : id={}, name={}", id, skill.getName());
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Transactional
    public void deactivate(Long id) {
        log.info("Début désactivation compétence : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        if (!skill.getActive()) {
            log.warn("Compétence déjà désactivée : id={}, name={}", id, skill.getName());
            return;
        }

        skill.setActive(false);
        skillRepository.save(skill);
        log.info("Compétence désactivée avec succès : id={}, name={}", id, skill.getName());
    }

    // ⚙️ MÉTHODE UTILITAIRE PRIVÉE
    /**
     * ⚙️ Crée un Pageable avec tri.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "name"; // ⚙️ Tri par défaut sur le nom pour les compétences
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * Normalise un nom de compétence :
     * - supprime les espaces en début/fin
     * - première lettre en majuscule, reste inchangé
     * Ex: "  java  " → "Java", "JAVA" → "JAVA" (inchangé sauf trim)
     * Pour forcer title-case : "java" → "Java"
     */
    private String normalize(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return trimmed;
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}