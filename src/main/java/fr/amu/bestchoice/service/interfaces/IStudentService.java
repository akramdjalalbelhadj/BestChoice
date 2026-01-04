package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface du service de gestion des profils étudiants.
 */
public interface IStudentService {

    /**
     * Crée un nouveau profil étudiant.
     *
     * @param userId L'ID de l'utilisateur
     * @param dto Les données de création
     * @return Le profil étudiant créé
     */
    StudentResponse create(Long userId, StudentCreateRequest dto);

    /**
     * Met à jour un profil étudiant.
     *
     * @param id L'ID du profil étudiant
     * @param dto Les données de mise à jour
     * @return Le profil étudiant mis à jour
     */
    StudentResponse update(Long id, StudentUpdateRequest dto);

    /**
     * Récupère un profil étudiant par son ID.
     *
     * @param id L'ID du profil étudiant
     * @return Le profil étudiant
     */
    StudentResponse findById(Long id);

    /**
     * Récupère tous les profils étudiants avec pagination.
     *
     * @param page Le numéro de page (commence à 0)
     * @param size La taille de la page
     * @param sortBy Le champ de tri
     * @param sortDirection La direction du tri (ASC ou DESC)
     * @return Une page de profils étudiants
     */
    Page<StudentResponse> findAll(int page, int size, String sortBy, String sortDirection);

    /**
     * Récupère tous les profils étudiants.
     *
     * @return La liste de tous les profils étudiants
     */
    List<StudentResponse> findAll();

    /**
     * Récupère tous les profils étudiants complets.
     *
     * @return La liste des profils étudiants complets
     */
    List<StudentResponse> findAllComplete();
}
