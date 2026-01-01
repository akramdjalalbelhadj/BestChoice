package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;

/**
 * Interface du service d'inscription des utilisateurs.
 */
public interface IRegisterService {

    /**
     * Inscrit un nouvel utilisateur et crée son profil associé.
     *
     * @param dto Les données d'inscription
     * @return Les informations de l'utilisateur inscrit
     */
    RegisterResponse register(RegisterRequest dto);
}
