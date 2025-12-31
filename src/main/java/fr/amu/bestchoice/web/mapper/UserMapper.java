package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper pour convertir entre l'entité User et ses DTOs.
 */
@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    // CREATE : RegisterRequest (dto ----> User

    /**
     * Convertit un RegisterRequest (DTO) en entité User + justificatif
     */
    @Mapping(target = "id", ignore = true)

    // Le mot de passe doit être hashé dans le service AVANT d'être mis dans User
    // Le DTO contient "password" en clair, l'entité contient "passwordHash" (hash BCrypt)
    @Mapping(target = "passwordHash", ignore = true)

    // Un nouveau compte est toujours actif par défaut
    @Mapping(target = "active", constant = "true")

    // Ce champ est géré automatiquement par @CreationTimestamp dans l'entité
    @Mapping(target = "createdAt", ignore = true)

    // Si l'utilisateur est un étudiant, on créera l'entité Student séparément
    @Mapping(target = "student", ignore = true)

    // Si l'utilisateur est un enseignant, on créera l'entité Teacher séparément
    @Mapping(target = "teacher", ignore = true)


    User toEntity(RegisterRequest dto);

    // UPDATE : UserUpdateRequest ---> User

    /**
     * Met à jour une entité User existante avec les données du DTO
     * Les champs null dans le DTO ne modifient PAS l'entité (grâce à IGNORE strategy)
     * + Justif
     */

    // On ne change JAMAIS l'ID d'un utilisateur existant
    @Mapping(target = "id", ignore = true)

    // Le mot de passe ne se change PAS via UserUpdateRequest (endpoint séparé avec PasswordChange)
    @Mapping(target = "passwordHash", ignore = true)

    // Le statut actif/inactif ne se change PAS ici (action admin séparée)
    @Mapping(target = "active", ignore = true)

    // Les rôles ne se modifient PAS après création (sécurité)
    @Mapping(target = "roles", ignore = true)

    // La date de création ne change JAMAIS
    @Mapping(target = "createdAt", ignore = true)

    // On ne touche pas aux relations Student/Teacher ici
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "teacher", ignore = true)

    void updateEntityFromDto(UserUpdateRequest dto, @MappingTarget User entity);

    // READ : User ---> UserResponse

    /**
     * Convertit une entité User en UserResponse (DTO).
     * Utilisé pour retourner les données utilisateur via l'API REST
     */
    UserResponse toResponse(User user);

    /**
     * Convertit une LISTE d'entités User en LISTE de UserResponse
     */
    List<UserResponse> toResponseList(List<User> entities);
}