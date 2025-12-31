package fr.amu.bestchoice.service;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeder de démarrage :
 * - Crée un compte ADMIN temporaire (si absent) pour tester l'API.
 * - À supprimer / remplacer plus tard (migration, création admin via script, etc.)
 */
@Slf4j // ✅ Logger SLF4J via Lombok
@Component
@RequiredArgsConstructor
public class Seeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ⚠️ Pour le dev uniquement
    private static final String ADMIN_EMAIL = "admin@bestchoice.local";
    private static final String ADMIN_PASSWORD = "Admin12345!";

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("ℹ️ Compte ADMIN déjà présent (email={})", ADMIN_EMAIL);
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("BestChoice")
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .active(true)
                .roles(Set.of(Role.ADMIN))
                .build();

        userRepository.save(admin);
        // ⚠️ DEV uniquement — à supprimer en prod
        log.warn(
                "⚠️ ADMIN DEV créé | email={} | password={}",
                ADMIN_EMAIL,
                ADMIN_PASSWORD
        );
    }
}
