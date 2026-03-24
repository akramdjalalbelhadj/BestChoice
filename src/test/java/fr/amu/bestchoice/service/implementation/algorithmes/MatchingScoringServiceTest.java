package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScoringServiceTest {

    private MatchingScoringService scoringService;
    private Student student;
    private Project project;
    private Subject subject;
    private MatchingCampaign campaign;

    @BeforeEach
    void setUp() {
        scoringService = new MatchingScoringService();
        
        student = new Student();
        project = new Project();
        subject = new Subject();
        campaign = new MatchingCampaign();
        
        campaign.setSkillsWeight(new BigDecimal("0.4"));
        campaign.setInterestsWeight(new BigDecimal("0.4"));
        campaign.setWorkTypeWeight(new BigDecimal("0.2"));
    }

    @Test
    void computeGlobalScore_ShouldReturnScoreBetween0And1() {
        // Given
        Skill skill1 = new Skill(); skill1.setName("S1");
        Skill skill2 = new Skill(); skill2.setName("S2");
        student.setSkills(Set.of(skill1));
        project.setRequiredSkills(Set.of(skill1, skill2)); // 50% skill score

        Keyword kw1 = new Keyword(); kw1.setLabel("K1");
        student.setInterests(Set.of(kw1));
        project.setKeywords(Set.of(kw1)); // 100% interest score

        student.setPreferredWorkTypes(Set.of(WorkType.DEVELOPPEMENT));
        project.setWorkTypes(Set.of(WorkType.DEVELOPPEMENT)); // 100% work type score

        // When
        // Global = 0.5*0.4 + 1.0*0.4 + 1.0*0.2 = 0.2 + 0.4 + 0.2 = 0.8
        BigDecimal score = scoringService.computeGlobalScore(student, project, subject, campaign);

        // Then
        assertThat(score).isEqualByComparingTo("0.8");
    }

    @Test
    void computeGlobalScore_ShouldHandleEmptySkillsAndInterests() {
        // When
        BigDecimal score = scoringService.computeGlobalScore(student, project, subject, campaign);

        // Then
        // Empty required/keywords return 0.5 by default
        // Global = 0.5*0.4 + 0.5*0.4 + 0.5*0.2 = 0.2 + 0.2 + 0.1 = 0.5
        assertThat(score).isEqualByComparingTo("0.5");
    }
}
