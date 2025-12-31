package fr.amu.bestchoice.service;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeder de données initiales.
 *
 * Rôle :
 * - Créer un compte ADMIN par défaut au démarrage de l'application
 * - UNIQUEMENT s'il n'existe pas déjà
 *
 * Utile pour :
 * - Démarrage du projet
 * - Tests CRUD
 * - Swagger / Postman
 */
@Component
@RequiredArgsConstructor
public class Seeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Email admin par défaut
        String adminEmail = "admin@bestchoice.fr";

        // Vérifie si un admin existe déjà
        if (userRepository.existsByEmail(adminEmail)) {
            return; // Rien à faire
        }

        // Création du compte admin
        User admin = User.builder()
                .firstName("Admin")
                .lastName("System")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("admin123"))
                .active(true)
                .roles(Set.of(Role.ADMIN))
                .build();

        userRepository.save(admin);

        System.out.println("✅ Compte ADMIN créé : " + adminEmail);
    }
}
