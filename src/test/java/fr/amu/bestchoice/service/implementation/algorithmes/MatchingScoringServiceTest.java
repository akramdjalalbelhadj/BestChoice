package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScoringServiceTest {

    private MatchingScoringService scoringService;
    private Student student;
    private Project project;
    private MatchingCampaign campaign;

    @BeforeEach
    void setUp() {
        scoringService = new MatchingScoringService();
        
        student = new Student();
        student.setSkills(new HashSet<>());
        student.setInterests(new HashSet<>());
        student.setPreferredWorkTypes(new HashSet<>());

        project = new Project();
        project.setRequiredSkills(new HashSet<>());
        project.setKeywords(new HashSet<>());
        project.setWorkTypes(new HashSet<>());

        campaign = new MatchingCampaign();
        campaign.setSkillsWeight(new BigDecimal("0.5"));
        campaign.setInterestsWeight(new BigDecimal("0.3"));
        campaign.setWorkTypeWeight(new BigDecimal("0.2"));
    }

    @Test
    void computeGlobalScore_ShouldReturnCorrectScore_WhenAllMatch() {
        Skill skill = new Skill();
        skill.setId(1L);
        student.getSkills().add(skill);
        project.getRequiredSkills().add(skill);

        Keyword keyword = new Keyword();
        keyword.setId(1L);
        student.getInterests().add(keyword);
        project.getKeywords().add(keyword);

        student.getPreferredWorkTypes().add(WorkType.DEVELOPPEMENT);
        project.getWorkTypes().add(WorkType.DEVELOPPEMENT);

        BigDecimal score = scoringService.computeGlobalScore(student, project, null, campaign);

        assertThat(score).isEqualTo(new BigDecimal("1.0000"));
    }

    @Test
    void computeGlobalScore_ShouldReturnDefaultScore_WhenNoRequirements() {
        BigDecimal score = scoringService.computeGlobalScore(student, project, null, campaign);
        // Default for skills = 0.5, interests = 0.5, workType = 0.5
        // 0.5 * 0.5 + 0.5 * 0.3 + 0.5 * 0.2 = 0.25 + 0.15 + 0.1 = 0.5
        assertThat(score).isEqualTo(new BigDecimal("0.5000"));
    }

    @Test
    void computeGlobalScore_ShouldReturnZero_WhenNoMatch() {
        Skill skill1 = new Skill(); skill1.setId(1L);
        Skill skill2 = new Skill(); skill2.setId(2L);
        student.getSkills().add(skill1);
        project.getRequiredSkills().add(skill2);

        Keyword k1 = new Keyword(); k1.setId(1L);
        Keyword k2 = new Keyword(); k2.setId(2L);
        student.getInterests().add(k1);
        project.getKeywords().add(k2);

        student.getPreferredWorkTypes().add(WorkType.DEVELOPPEMENT);
        project.getWorkTypes().add(WorkType.RECHERCHE);

        BigDecimal score = scoringService.computeGlobalScore(student, project, null, campaign);
        assertThat(score).isEqualTo(new BigDecimal("0.0000"));
    }
}
