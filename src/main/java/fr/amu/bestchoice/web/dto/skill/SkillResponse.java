package fr.amu.bestchoice.web.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record SkillResponse (

     Long id,
     // Ex: "Java"- "Python"- "SQL"
     String name,
     String description,
     // Ex: "Programmation", "Base de Données", "AI"
     String category,
     // Niveau de difficulté ou d'expertise (1--->5)
     Integer level,
     Boolean active
){}
