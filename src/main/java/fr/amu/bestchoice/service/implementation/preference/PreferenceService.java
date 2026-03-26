package fr.amu.bestchoice.service.implementation.preference;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.service.interfaces.IPreferenceService;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.StudentPreferenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des préférences étudiantes (StudentPreferences).
 *
 * Opérations disponibles :
 * - Créer une nouvelle préférence (choix de projet par un étudiant)
 * - Récupérer les préférences d'un étudiant
 * - Récupérer les préférences pour un projet
 * - Supprimer une préférence
 * - Changer le statut d'une préférence
 *
 * RÈGLES MÉTIER :
 * - Un étudiant peut avoir maximum 10 préférences
 * - Un étudiant ne peut pas choisir 2 fois le même projet
 * - Un étudiant ne peut pas utiliser 2 fois le même rang (1er choix, 2ème choix, etc.)
 * - Le rang doit être entre 1 et 10
 * - On ne peut pas choisir un projet inactif ou complet
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceService implements IPreferenceService {

    private final StudentPreferenceRepository preferenceRepository;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final SubjectRepository subjectRepository;
    private final MatchingCampaignRepository campaignRepository;
    private final StudentPreferenceMapper preferenceMapper;


    // Constante : nombre maximum de préférences par étudiant
    private static final int MAX_PREFERENCES_PER_STUDENT = 10;

    // ==================== CREATE ====================

    /**
     * Crée une nouvelle préférence pour un étudiant.
     *
     * @param dto Les données de la préférence (studentId, projectId, rank, motivation, comment)
     * @return PreferenceResponse avec les données de la préférence créée
     * @throws NotFoundException Si l'étudiant ou le projet n'existe pas
     * @throws BusinessException Si une règle métier est violée
     */
    @Transactional
    public PreferenceResponse create(PreferenceCreateRequest dto) {
        // 1. Récupération des entités de base
        Student student = studentRepository.findById(dto.studentId())
                .orElseThrow(() -> new NotFoundException("Étudiant introuvable"));

        // 2. Vérification des règles métier
        if (preferenceRepository.existsByStudentIdAndRank(dto.studentId(), dto.rank())) {
            throw new BusinessException("Ce rang est déjà utilisé par l'étudiant");
        }
        if (dto.projectId() != null && preferenceRepository.existsByStudentIdAndProjectId(dto.studentId(), dto.projectId())) {
            throw new BusinessException("L'étudiant a déjà soumis un vœu pour ce projet");
        }

        // On récupère la campagne (indispensable pour le contexte)
        MatchingCampaign campaign = campaignRepository.findById(dto.campaignId())
                .orElseThrow(() -> new NotFoundException("Campagne introuvable"));

        // 3. Préparation de l'entité
        StudentPreference preference = preferenceMapper.toEntity(dto);
        preference.setStudent(student);
        preference.setMatchingCampaign(campaign);
        preference.setStatus(PreferenceStatus.PENDING);

        // 4. Gestion de l'item (Projet ou Matière)
        if (dto.projectId() != null) {
            Project project = projectRepository.findById(dto.projectId())
                    .orElseThrow(() -> new NotFoundException("Projet introuvable"));
            preference.setProject(project);
        } else if (dto.subjectId() != null) {
            Subject subject = subjectRepository.findById(dto.subjectId())
                    .orElseThrow(() -> new NotFoundException("Matière introuvable"));
            preference.setSubject(subject);
        } else {
            throw new BusinessException("Un projet ou une matière doit être sélectionné.");
        }

        return preferenceMapper.toResponse(preferenceRepository.save(preference));
    }

    // ==================== READ ====================

    /**
     * Récupère toutes les préférences d'un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @return Liste de PreferenceResponse triée par rang (1er choix, 2ème choix, etc.)
     * @throws NotFoundException Si l'étudiant n'existe pas
     */
    public List<PreferenceResponse> findByStudentId(Long studentId) {

        log.debug("Recherche des préférences de l'étudiant : studentId={}", studentId);

        // Vérifier que l'étudiant existe
        if (!studentRepository.existsById(studentId)) {
            log.error("Étudiant introuvable : studentId={}", studentId);
            throw new NotFoundException("Étudiant introuvable avec l'ID : " + studentId);
        }

        // Récupérer les préférences triées par rang
        List<StudentPreference> preferences = preferenceRepository.findByStudentIdOrderByRankAsc(studentId);

        log.info("Préférences trouvées pour l'étudiant {} : {} préférence(s)", studentId, preferences.size());

        return preferenceMapper.toResponseList(preferences);
    }

    /**
     * Récupère toutes les préférences pour un projet.
     *
     * @param projectId L'ID du projet
     * @return Liste de PreferenceResponse
     * @throws NotFoundException Si le projet n'existe pas
     */
    public List<PreferenceResponse> findByProjectId(Long projectId) {

        log.debug("Recherche des préférences pour le projet : projectId={}", projectId);

        // Vérifier que le projet existe
        if (!projectRepository.existsById(projectId)) {
            log.error("Projet introuvable : projectId={}", projectId);
            throw new NotFoundException("Projet introuvable avec l'ID : " + projectId);
        }

        // ✅ CORRIGÉ : Utiliser findByProjectIdOrderByRankAsc au lieu de findByProjectId
        List<StudentPreference> preferences = preferenceRepository.findByProjectIdOrderByRankAsc(projectId);

        log.info("Préférences trouvées pour le projet {} : {} préférence(s)", projectId, preferences.size());

        return preferenceMapper.toResponseList(preferences);
    }

    /**
     * Récupère une préférence par son ID.
     *
     * @param id L'ID de la préférence
     * @return PreferenceResponse
     * @throws NotFoundException Si la préférence n'existe pas
     */
    public PreferenceResponse findById(Long id) {

        log.debug("Recherche préférence par ID : id={}", id);

        StudentPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Préférence introuvable : id={}", id);
                    return new NotFoundException("Préférence introuvable avec l'ID : " + id);
                });

        log.debug("Préférence trouvée : studentId={}, projectId={}, rank={}",
                preference.getStudent().getId(), preference.getProject().getId(), preference.getRank());

        return preferenceMapper.toResponse(preference);
    }

    @Override
    public List<PreferenceResponse> findByStudentIdAndCampaignId(Long studentId, Long campaignId) {
        log.debug("Recherche des vœux pour l'étudiant {} dans la campagne {}", studentId, campaignId);

        // Vérification rapide de l'existence de l'étudiant (optionnel mais recommandé)
        if (!studentRepository.existsById(studentId)) {
            throw new NotFoundException("Étudiant introuvable");
        }

        List<StudentPreference> preferences = preferenceRepository
                .findByStudentIdAndMatchingCampaignIdOrderByRankAsc(studentId, campaignId);

        log.info("{} vœux trouvés pour la campagne {}", preferences.size(), campaignId);

        return preferenceMapper.toResponseList(preferences);
    }

    // ==================== DELETE ====================

    /**
     * Supprime une préférence.
     *
     * IMPORTANT : Un étudiant peut supprimer une de ses préférences SEULEMENT si
     * elle a le statut PENDING (en attente).
     *
     * @param id L'ID de la préférence à supprimer
     * @throws NotFoundException Si la préférence n'existe pas
     * @throws BusinessException Si la préférence n'est pas en statut PENDING
     */
    @Transactional
    public void delete(Long id) {

        log.info("Début suppression préférence : id={}", id);

        // Vérifier que la préférence existe
        StudentPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Préférence introuvable : id={}", id);
                    return new NotFoundException("Préférence introuvable avec l'ID : " + id);
                });

        // Vérifier que la préférence est en statut PENDING
        if (preference.getStatus() != PreferenceStatus.PENDING) {
            log.warn("Tentative de suppression d'une préférence avec statut {} : id={}",
                    preference.getStatus(), id);
            throw new BusinessException("Vous ne pouvez supprimer que les préférences en attente");
        }

        log.debug("Préférence à supprimer : studentId={}, projectId={}, rank={}",
                preference.getStudent().getId(), preference.getProject().getId(), preference.getRank());

        // Supprimer la préférence
        preferenceRepository.delete(preference);

        log.info("Préférence supprimée avec succès : id={}", id);
    }

    // ==================== CHANGEMENT DE STATUT ====================

    /**
     * Change le statut d'une préférence.
     *
     * Cette méthode est utilisée par l'algorithme de matching ou par l'admin.
     *
     * Statuts possibles :
     * - PENDING : En attente (initial)
     * - ACCEPTED : Acceptée (étudiant assigné à ce projet)
     * - REJECTED : Rejetée (étudiant non assigné)
     *
     * @param id L'ID de la préférence
     * @param newStatus Le nouveau statut
     * @return PreferenceResponse avec le nouveau statut
     * @throws NotFoundException Si la préférence n'existe pas
     */
    @Transactional
    public PreferenceResponse changeStatus(Long id, PreferenceStatus newStatus) {

        log.info("Début changement statut préférence : id={}, newStatus={}", id, newStatus);

        StudentPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Préférence introuvable : id={}", id);
                    return new NotFoundException("Préférence introuvable avec l'ID : " + id);
                });

        PreferenceStatus oldStatus = preference.getStatus();

        // Si le statut ne change pas, ne rien faire
        if (oldStatus == newStatus) {
            log.debug("Statut inchangé : id={}, status={}", id, oldStatus);
            return preferenceMapper.toResponse(preference);
        }

        // Changer le statut
        preference.setStatus(newStatus);
        StudentPreference updatedPreference = preferenceRepository.save(preference);

        log.info("Statut de la préférence changé avec succès : id={}, oldStatus={}, newStatus={}",
                id, oldStatus, newStatus);

        return preferenceMapper.toResponse(updatedPreference);
    }

    /**
     * Accepte une préférence (PENDING → ACCEPTED).
     *
     * @param id L'ID de la préférence
     * @return PreferenceResponse
     */
    @Transactional
    public PreferenceResponse accept(Long id) {
        log.info("Acceptation de la préférence : id={}", id);
        return changeStatus(id, PreferenceStatus.ACCEPTED);
    }

    /**
     * Rejette une préférence (PENDING → REJECTED).
     *
     * @param id L'ID de la préférence
     * @return PreferenceResponse
     */
    @Transactional
    public PreferenceResponse reject(Long id) {
        log.info("Rejet de la préférence : id={}", id);
        return changeStatus(id, PreferenceStatus.REJECTED);
    }
}