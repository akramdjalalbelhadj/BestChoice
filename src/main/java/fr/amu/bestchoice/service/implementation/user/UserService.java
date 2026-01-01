package fr.amu.bestchoice.service.implementation.user;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.service.interfaces.IUserService;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== CREATE ====================

    @Transactional
    public RegisterResponse register(RegisterRequest dto) {
        log.info("Début inscription utilisateur : email={}", dto.email());

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Tentative d'inscription avec un email existant : email={}", dto.email());
            throw new BusinessException("Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
        }

        if (dto.studentNumber() != null && userRepository.existsByStudentNumber(dto.studentNumber())) {
            log.warn("Tentative d'inscription avec un numéro étudiant existant : studentNumber={}", dto.studentNumber());
            throw new BusinessException("Un utilisateur avec le numéro étudiant '" + dto.studentNumber() + "' existe déjà");
        }

        User user = userMapper.toEntity(dto);
        log.debug("User mappé : email={}", user.getEmail());

        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPassword);
        log.debug("Mot de passe hashé avec succès");

        User savedUser = userRepository.save(user);
        log.info("Utilisateur créé avec succès : id={}, email={}, roles={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRoles());

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    // ==================== UPDATE ====================

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest dto) {
        log.info("Début mise à jour utilisateur : id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : id={}", id);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + id);
                });

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                log.warn("Tentative de modifier vers un email existant : oldEmail={}, newEmail={}",
                        user.getEmail(), dto.email());
                throw new BusinessException("Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
            }
        }

        if (dto.studentNumber() != null && !dto.studentNumber().equals(user.getStudentNumber())) {
            if (userRepository.existsByStudentNumber(dto.studentNumber())) {
                log.warn("Tentative de modifier vers un numéro étudiant existant : newStudentNumber={}", dto.studentNumber());
                throw new BusinessException("Un utilisateur avec le numéro étudiant '" + dto.studentNumber() + "' existe déjà");
            }
        }

        userMapper.updateEntityFromDto(dto, user);
        log.debug("Utilisateur après mise à jour : email={}", user.getEmail());

        User updatedUser = userRepository.save(user);
        log.info("Utilisateur mis à jour avec succès : id={}, email={}", updatedUser.getId(), updatedUser.getEmail());

        return userMapper.toResponse(updatedUser);
    }

    // ==================== READ ====================

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

    // ⚙️ NOUVELLE MÉTHODE PAGINÉE
    /**
     * ⚙️ Récupère tous les utilisateurs avec pagination.
     */
    public Page<UserResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération utilisateurs paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<User> usersPage = userRepository.findAll(pageable);

        log.info("⚙️ Page d'utilisateurs récupérée : page={}/{}, total={}",
                usersPage.getNumber() + 1, usersPage.getTotalPages(), usersPage.getTotalElements());

        return usersPage.map(userMapper::toResponse);
    }

    // ANCIENNE MÉTHODE (rétrocompatibilité)
    public List<UserResponse> findAll() {
        log.debug("Récupération de tous les utilisateurs");
        List<User> users = userRepository.findAll();
        log.info("Nombre d'utilisateurs trouvés : {}", users.size());
        return userMapper.toResponseList(users);
    }

    public List<UserResponse> findAllActive() {
        log.debug("Récupération des utilisateurs actifs uniquement");
        List<User> users = userRepository.findByActiveTrue();
        log.info("Nombre d'utilisateurs actifs trouvés : {}", users.size());
        return userMapper.toResponseList(users);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

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

    // ⚙️ MÉTHODE UTILITAIRE PRIVÉE
    /**
     * ⚙️ Crée un Pageable avec tri.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}