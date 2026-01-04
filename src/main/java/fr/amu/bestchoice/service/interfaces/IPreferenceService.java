package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;

import java.util.List;

/**
 * Interface du service de gestion des préférences étudiantes.
 */
public interface IPreferenceService {

    /**
     * Crée une nouvelle préférence.
     *
     * @param dto Les données de création
     * @return La préférence créée
     */
    PreferenceResponse create(PreferenceCreateRequest dto);

    /**
     * Récupère toutes les préférences d'un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @return La liste des préférences de l'étudiant
     */
    List<PreferenceResponse> findByStudentId(Long studentId);

    /**
     * Récupère toutes les préférences pour un projet.
     *
     * @param projectId L'ID du projet
     * @return La liste des préférences pour le projet
     */
    List<PreferenceResponse> findByProjectId(Long projectId);

    /**
     * Récupère une préférence par son ID.
     *
     * @param id L'ID de la préférence
     * @return La préférence
     */
    PreferenceResponse findById(Long id);

    /**
     * Supprime une préférence.
     *
     * @param id L'ID de la préférence
     */
    void delete(Long id);

    /**
     * Change le statut d'une préférence.
     *
     * @param id L'ID de la préférence
     * @param newStatus Le nouveau statut
     * @return La préférence mise à jour
     */
    PreferenceResponse changeStatus(Long id, PreferenceStatus newStatus);

    /**
     * Accepte une préférence (PENDING → ACCEPTED).
     *
     * @param id L'ID de la préférence
     * @return La préférence acceptée
     */
    PreferenceResponse accept(Long id);

    /**
     * Rejette une préférence (PENDING → REJECTED).
     *
     * @param id L'ID de la préférence
     * @return La préférence rejetée
     */
    PreferenceResponse reject(Long id);
}
