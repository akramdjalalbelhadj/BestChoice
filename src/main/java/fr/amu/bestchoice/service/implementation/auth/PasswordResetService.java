package fr.amu.bestchoice.service.implementation.auth;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.auth.ForgotPasswordRequest;
import fr.amu.bestchoice.web.dto.auth.ResetPasswordRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.reset-token.expiry-minutes:30}")
    private int expiryMinutes;

    // ── Étape 1 : demande de réinitialisation ────────────────────────────────

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException(
                        "Cet email n'existe pas dans la base de données. Contactez l'administrateur."));

        // Génération du token unique
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(expiryMinutes));
        userRepository.save(user);

        // Lien toujours visible dans la console (dev)
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        log.info("================================================================");
        log.info("🔑 LIEN DE RÉINITIALISATION pour {} :", email);
        log.info("👉 {}", resetLink);
        log.info("================================================================");

        // Envoi de l'email (optionnel — ne bloque pas si SMTP non configuré)
        sendResetEmail(user.getEmail(), user.getFirstName(), token, resetLink);
        log.info("✅ Email de réinitialisation envoyé à : {}", email);
    }

    // ── Étape 2 : réinitialisation du mot de passe ───────────────────────────

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas.");
        }

        User user = userRepository.findByResetToken(request.token())
                .orElseThrow(() -> new BusinessException("Lien de réinitialisation invalide ou expiré."));

        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new BusinessException("Ce lien a expiré. Veuillez refaire une demande.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur : {}", user.getEmail());
    }

    // ── Email ─────────────────────────────────────────────────────────────────

    private void sendResetEmail(String to, String firstName, String token, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("BestChoice — Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour " + firstName + ",\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe :\n" +
                resetLink + "\n\n" +
                "Ce lien est valable " + expiryMinutes + " minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                "— L'équipe BestChoice"
        );

        try {
            mailSender.send(message);
            log.info("📧 Email envoyé avec succès à : {}", to);
        } catch (Exception e) {
            log.warn("⚠️ Envoi email échoué pour {} : {}. Utilise le lien dans la console.", to, e.getMessage());
            // Ne bloque pas — le lien est déjà affiché dans la console
        }
    }
}
