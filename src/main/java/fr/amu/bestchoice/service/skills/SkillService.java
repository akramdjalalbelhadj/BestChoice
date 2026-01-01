package fr.amu.bestchoice.service.skills;

import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des compétences (Skills).
 *
 * Opérations disponibles :
 * - Créer une nouvelle compétence
 * - Modifier une compétence existante
 * - Récupérer une compétence par ID
 * - Récupérer toutes les compétences
 * - Récupérer les compétences actives uniquement
 * - Supprimer une compétence
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {


    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    /**
     * Crée une nouvelle compétence.
     */
    @Transactional
    public SkillResponse create(SkillCreateRequest dto) {
        log.info("Début création compétence : name={}", dto.name());

        // Vérifier qu'une compétence avec ce nom n'existe pas déjà
        if (skillRepository.existsByName(dto.name())) {
            log.warn("Tentative de création d'une compétence existante : name={}", dto.name());
            throw new BusinessException("Une compétence avec le nom '" + dto.name() + "' existe déjà");
        }

        Skill skill = skillMapper.toEntity(dto);
        log.debug("Skill mappée : {}", skill);

        Skill savedSkill = skillRepository.save(skill);
        log.info("Compétence créée avec succès : id={}, name={}", savedSkill.getId(), savedSkill.getName());

        return skillMapper.toResponse(savedSkill);
    }


    /**
     * Met à jour une compétence existante.
     * Les champs null dans le DTO ne sont pas modifiés (stratégie IGNORE).
     */
    @Transactional
    public SkillResponse update(Long id, SkillUpdateRequest dto) {
        log.info("Début mise à jour compétence : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        log.debug("Compétence trouvée : name={}", skill.getName());

        // ===== VALIDATION MÉTIER =====

        // Si le nom change, vérifier qu'il n'est pas déjà utilisé par une autre compétence
        if (dto.name() != null && !dto.name().equals(skill.getName())) {
            if (skillRepository.existsByName(dto.name())) {
                log.warn("Tentative de renommer vers un nom existant : oldName={}, newName={}",
                        skill.getName(), dto.name());
                throw new BusinessException("Une compétence avec le nom '" + dto.name() + "' existe déjà");
            }
        }

        // ===== MAPPING DTO → ENTITY =====

        // Le mapper met à jour uniquement les champs non null
        skillMapper.updateEntityFromDto(dto, skill);
        log.debug("Compétence après mise à jour : {}", skill);

        // ===== SAUVEGARDE =====
        Skill updatedSkill = skillRepository.save(skill);
        log.info("Compétence mise à jour avec succès : id={}, name={}", updatedSkill.getId(), updatedSkill.getName());

        // ===== MAPPING ENTITY → DTO =====

        return skillMapper.toResponse(updatedSkill);
    }



    /**
     * Récupère une compétence par son ID.
     */
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

    /**
     * Récupère toutes les compétences.
     */
    public List<SkillResponse> findAll() {

        log.debug("Récupération de toutes les compétences");

        List<Skill> skills = skillRepository.findAll();
        log.info("Nombre de compétences trouvées : {}", skills.size());
        return skillMapper.toResponseList(skills);
    }

    /**
     * Récupère uniquement les compétences actives.
     */
    public List<SkillResponse> findAllActive() {

        log.debug("Récupération des compétences actives uniquement");

        List<Skill> skills = skillRepository.findByActiveTrue();
        log.info("Nombre de compétences actives trouvées : {}", skills.size());
        return skillMapper.toResponseList(skills);
    }


    /**
     * Supprime une compétence.
     *
     * IMPORTANT : Suppression physique (hard delete).
     * Pour une suppression logique (soft delete), utiliser update avec active=false.
     */
    @Transactional
    public void delete(Long id) {

        log.info("Début suppression compétence : id={}", id);

        // Vérifier que la compétence existe
        if (!skillRepository.existsById(id)) {
            log.error("Tentative de suppression d'une compétence inexistante : id={}", id);
            throw new NotFoundException("Compétence introuvable avec l'ID : " + id);
        }

        // Supprimer la compétence
        skillRepository.deleteById(id);
        log.info("Compétence supprimée avec succès : id={}", id);
    }

    /**
     * Désactive une compétence (soft delete).
     * La compétence reste en base mais n'est plus visible.
     */
    @Transactional
    public void deactivate(Long id) {

        log.info("Début désactivation compétence : id={}", id);

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Compétence introuvable : id={}", id);
                    return new NotFoundException("Compétence introuvable avec l'ID : " + id);
                });

        // Désactiver la compétence
        skill.setActive(false);
        skillRepository.save(skill);

        log.info("Compétence désactivée avec succès : id={}, name={}", id, skill.getName());
    }
}