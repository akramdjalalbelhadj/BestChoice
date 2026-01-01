package fr.amu.bestchoice.service.user;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des utilisateurs (Users).
 *
 * Opérations disponibles :
 * - Créer un utilisateur (inscription admin)
 * - Modifier un utilisateur
 * - Récupérer un utilisateur par ID
 * - Récupérer tous les utilisateurs
 * - Activer/Désactiver un utilisateur
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // ==================== DÉPENDANCES ====================

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== CREATE ====================

    /**
     * Crée un nouvel utilisateur (inscription par l'admin).
     *
     * IMPORTANT : Cette méthode est utilisée par l'admin pour créer des comptes.
     * Les rôles sont définis par l'admin dans RegisterRequest.
     *
     * @param dto Les données du nouvel utilisateur
     * @return RegisterResponse avec les données de l'utilisateur créé
     * @throws BusinessException Si l'email ou le numéro étudiant existe déjà
     */
    @Transactional
    public RegisterResponse register(RegisterRequest dto) {

        log.info("Début inscription utilisateur : email={}", dto.email());

        // ===== VALIDATION MÉTIER =====

        // Vérifier que l'email n'est pas déjà utilisé
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Tentative d'inscription avec un email existant : email={}", dto.email());
            throw new BusinessException("Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
        }

        // Vérifier que le numéro étudiant n'est pas déjà utilisé (si fourni)
        if (dto.studentNumber() != null && userRepository.existsByStudentNumber(dto.studentNumber())) {
            log.warn("Tentative d'inscription avec un numéro étudiant existant : studentNumber={}", dto.studentNumber());
            throw new BusinessException("Un utilisateur avec le numéro étudiant '" + dto.studentNumber() + "' existe déjà");
        }

        // ===== MAPPING DTO → ENTITY =====

        User user = userMapper.toEntity(dto);

        log.debug("User mappé : email={}", user.getEmail());

        // ===== HACHAGE DU MOT DE PASSE =====

        // Hasher le mot de passe avec BCrypt
        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPassword);

        log.debug("Mot de passe hashé avec succès");

        // ===== SAUVEGARDE =====

        User savedUser = userRepository.save(user);

        log.info("Utilisateur créé avec succès : id={}, email={}, roles={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRoles());

        // ===== MAPPING ENTITY → DTO =====

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un utilisateur existant.
     *
     * ATTENTION : Ne modifie que les informations d'identité (nom, prénom, email).
     * Les rôles et le mot de passe ne peuvent pas être modifiés via cette méthode.
     */
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest dto) {

        log.info("Début mise à jour utilisateur : id={}", id);

        // ===== RÉCUPÉRATION =====

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : id={}", id);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + id);
                });

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        // ===== VALIDATION MÉTIER =====

        // Si l'email change, vérifier qu'il n'est pas déjà utilisé
        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                log.warn("Tentative de modifier vers un email existant : oldEmail={}, newEmail={}",
                        user.getEmail(), dto.email());
                throw new BusinessException("Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
            }
        }

        // Si le numéro étudiant change, vérifier qu'il n'est pas déjà utilisé
        if (dto.studentNumber() != null && !dto.studentNumber().equals(user.getStudentNumber())) {
            if (userRepository.existsByStudentNumber(dto.studentNumber())) {
                log.warn("Tentative de modifier vers un numéro étudiant existant : newStudentNumber={}", dto.studentNumber());
                throw new BusinessException("Un utilisateur avec le numéro étudiant '" + dto.studentNumber() + "' existe déjà");
            }
        }

        // ===== MAPPING DTO → ENTITY =====

        userMapper.updateEntityFromDto(dto, user);

        log.debug("Utilisateur après mise à jour : email={}", user.getEmail());

        // ===== SAUVEGARDE =====

        User updatedUser = userRepository.save(user);

        log.info("Utilisateur mis à jour avec succès : id={}, email={}", updatedUser.getId(), updatedUser.getEmail());

        // ===== MAPPING ENTITY → DTO =====

        return userMapper.toResponse(updatedUser);
    }

    // ==================== READ ====================

    /**
     * Récupère un utilisateur par son ID.
     */
    public UserResponse findById(Long id) {

        log.debug("Recherche utilisateur par ID : id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : id={}", id);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + id);
                });

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        return userMapper.toResponse(user);
    }

    /**
     * Récupère tous les utilisateurs.
     */
    public List<UserResponse> findAll() {

        log.debug("Récupération de tous les utilisateurs");

        List<User> users = userRepository.findAll();

        log.info("Nombre d'utilisateurs trouvés : {}", users.size());

        return userMapper.toResponseList(users);
    }

    /**
     * Récupère uniquement les utilisateurs actifs.
     */
    public List<UserResponse> findAllActive() {

        log.debug("Récupération des utilisateurs actifs uniquement");

        List<User> users = userRepository.findByActiveTrue();

        log.info("Nombre d'utilisateurs actifs trouvés : {}", users.size());

        return userMapper.toResponseList(users);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    /**
     * Désactive un utilisateur.
     */
    @Transactional
    public void deactivate(Long id) {

        log.info("Début désactivation utilisateur : id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : id={}", id);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + id);
                });

        if (!user.getActive()) {
            log.warn("Utilisateur déjà désactivé : id={}, email={}", id, user.getEmail());
            return;
        }

        user.setActive(false);
        userRepository.save(user);

        log.info("Utilisateur désactivé avec succès : id={}, email={}", id, user.getEmail());
    }

    /**
     * Active un utilisateur.
     */
    @Transactional
    public void activate(Long id) {

        log.info("Début activation utilisateur : id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : id={}", id);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + id);
                });

        if (user.getActive()) {
            log.warn("Utilisateur déjà actif : id={}, email={}", id, user.getEmail());
            return;
        }

        user.setActive(true);
        userRepository.save(user);

        log.info("Utilisateur activé avec succès : id={}, email={}", id, user.getEmail());
    }
}