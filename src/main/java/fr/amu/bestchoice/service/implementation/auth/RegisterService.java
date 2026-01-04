package fr.amu.bestchoice.service.implementation.auth;

import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.service.interfaces.IRegisterService;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service dédié à l'inscription des utilisateurs.
 *
 * Ce service orchestre la création d'un utilisateur ET son profil associé
 * (Student ou Teacher) selon le rôle défini.
 *
 * Flux d'inscription :
 * 1. Créer l'utilisateur (User)
 * 2. Si ETUDIANT : créer un profil Student vide
 * 3. Si ENSEIGNANT : créer un profil Teacher vide
 * 4. Si ADMIN : ne rien faire de plus
 *
 * IMPORTANT : Seul un ADMIN peut créer des comptes via ce service.
 * Il n'y a pas d'inscription publique.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegisterService implements IRegisterService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== INSCRIPTION ====================

    /**
     * Inscrit un nouvel utilisateur et crée son profil associé si nécessaire.
     *
     * @param dto Les données d'inscription
     * @return RegisterResponse avec l'ID et les infos de base de l'utilisateur
     * @throws BusinessException Si l'email ou le numéro étudiant existe déjà
     */
    public RegisterResponse register(RegisterRequest dto) {

        log.info("Début inscription utilisateur : email={}, roles={}", dto.email(), dto.roles());

        // ===== VALIDATION EMAIL =====

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Tentative d'inscription avec un email existant : email={}", dto.email());
            throw new BusinessException("Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
        }

        // ===== VALIDATION NUMÉRO ÉTUDIANT =====

        if (dto.studentNumber() != null && userRepository.existsByStudentNumber(dto.studentNumber())) {
            log.warn("Tentative d'inscription avec un numéro étudiant existant : studentNumber={}", dto.studentNumber());
            throw new BusinessException("Un utilisateur avec le numéro étudiant '" + dto.studentNumber() + "' existe déjà");
        }

        // ===== CRÉATION UTILISATEUR =====

        User user = userMapper.toEntity(dto);

        // Hasher le mot de passe
        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPasswordHash(hashedPassword);

        log.debug("User mappé : email={}, roles={}", user.getEmail(), user.getRoles());

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        log.info("Utilisateur créé avec succès : id={}, email={}, roles={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRoles());

        // ===== CRÉATION PROFIL ASSOCIÉ =====

        createAssociatedProfile(savedUser);

        // ===== RETOUR =====

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName()
        );
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Crée le profil associé à l'utilisateur selon son rôle.
     *
     * - Si ETUDIANT : créer un Student vide
     * - Si ENSEIGNANT : créer un Teacher vide
     * - Si ADMIN : ne rien faire
     *
     * @param user L'utilisateur créé
     */
    private void createAssociatedProfile(User user) {

        // Vérifier si l'utilisateur a le rôle ETUDIANT
        if (user.getRoles().contains(Role.ETUDIANT)) {
            createStudentProfile(user);
        }

        // Vérifier si l'utilisateur a le rôle ENSEIGNANT
        if (user.getRoles().contains(Role.ENSEIGNANT)) {
            createTeacherProfile(user);
        }

        // Si ADMIN uniquement, ne rien faire de plus
        if (user.getRoles().contains(Role.ADMIN) &&
                !user.getRoles().contains(Role.ETUDIANT) &&
                !user.getRoles().contains(Role.ENSEIGNANT)) {
            log.debug("Utilisateur ADMIN créé sans profil associé : userId={}", user.getId());
        }
    }

    /**
     * Crée un profil Student vide pour l'utilisateur.
     *
     * Le profil sera complété plus tard par l'étudiant via StudentService.
     *
     * @param user L'utilisateur
     */
    private void createStudentProfile(User user) {

        log.debug("Création du profil étudiant pour l'utilisateur : userId={}", user.getId());

        Student student = Student.builder()
                .id(user.getId())  // L'ID du Student = ID du User (@MapsId)
                .user(user)
                .profileComplete(false)  // Profil incomplet par défaut
                .build();

        studentRepository.save(student);

        log.info("Profil étudiant créé (vide) : userId={}", user.getId());
    }

    /**
     * Crée un profil Teacher vide pour l'utilisateur.
     *
     * Le profil sera complété plus tard par l'enseignant via TeacherService.
     *
     * @param user L'utilisateur
     */
    private void createTeacherProfile(User user) {

        log.debug("Création du profil enseignant pour l'utilisateur : userId={}", user.getId());

        Teacher teacher = Teacher.builder()
                .id(user.getId())  // L'ID du Teacher = ID du User (@MapsId)
                .user(user)
                .build();

        teacherRepository.save(teacher);

        log.info("Profil enseignant créé (vide) : userId={}", user.getId());
    }
}