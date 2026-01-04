package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;

/**
 * Interface du service d'authentification.
 */
public interface IAuthService {

    /**
     * Authentifie un utilisateur et génère un JWT.
     *
     * @param request Les données de connexion (email, password)
     * @return Les informations de connexion avec le token JWT
     */
    LoginResponse login(LoginRequest request);
}
