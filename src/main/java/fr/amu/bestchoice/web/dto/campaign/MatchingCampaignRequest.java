package fr.amu.bestchoice.web.dto.campaign;

import fr.amu.bestchoice.model.entity.MatchingCampaignType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MatchingCampaignRequest(

        @NotBlank
        String name,

        String description,

        @Size(max = 9)
        String academicYear,

        @Min(1)
        @Max(2)
        Integer semester,

        @NotNull
        MatchingCampaignType campaignType,

        @NotNull
        MatchingAlgorithmType algorithmType,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal skillsWeight,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal workTypeWeight,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal interestsWeight,

        @NotNull Long teacherId
) {}