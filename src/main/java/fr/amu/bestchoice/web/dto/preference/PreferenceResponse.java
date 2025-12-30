package fr.amu.bestchoice.web.dto.preference;

import fr.amu.bestchoice.model.enums.PreferenceStatus;

import java.time.LocalDateTime;

public record PreferenceResponse(
        Long id,
        Long studentId,
        Long projectId,
        Integer rank,
        PreferenceStatus status,
        LocalDateTime createdAt
) {}
