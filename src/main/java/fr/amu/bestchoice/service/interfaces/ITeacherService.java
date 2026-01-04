package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des profils enseignants.
 */
public interface ITeacherService {

    /**
     * Crée un nouveau profil enseignant.
     *
     * @param userId L'ID de l'utilisateur
     * @param dto Les données de création
     * @return Le profil enseignant créé
     */
    TeacherResponse create(Long userId, TeacherCreateRequest dto);

    /**
     * Met à jour un profil enseignant.
     *
     * @param id L'ID du profil enseignant
     * @param dto Les données de mise à jour
     * @return Le profil enseignant mis à jour
     */
    TeacherResponse update(Long id, TeacherUpdateRequest dto);

    /**
     * Récupère un profil enseignant par son ID.
     *
     * @param id L'ID du profil enseignant
     * @return Le profil enseignant
     */
    TeacherResponse findById(Long id);

    /**
     * Récupère tous les profils enseignants avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page de profils enseignants
     */
    Page<TeacherResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère tous les profils enseignants.
     *
     * @return La liste de tous les profils enseignants
     */
    List<TeacherResponse> findAll();
}
