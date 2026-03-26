package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.WorkType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Service
public class MatchingScoringService {

    public BigDecimal computeGlobalScore(Student s, Project p, Subject sub, MatchingCampaign camp) {
        BigDecimal skills = computeSkillsScore(s, p, sub);
        BigDecimal interests = computeInterestsScore(s, p, sub);
        BigDecimal workType = computeWorkTypeScore(s, p, sub);

        return skills.multiply(camp.getSkillsWeight())
                .add(interests.multiply(camp.getInterestsWeight()))
                .add(workType.multiply(camp.getWorkTypeWeight()))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal computeSkillsScore(Student s, Project p, Subject sub) {
        Set<Skill> required = (p != null) ? p.getRequiredSkills() : sub.getRequiredSkills();
        if (required == null || required.isEmpty()) return new BigDecimal("0.5");

        Set<Long> requiredIds = required.stream().map(Skill::getId).collect(java.util.stream.Collectors.toSet());
        long match = s.getSkills().stream().filter(sk -> requiredIds.contains(sk.getId())).count();
        return BigDecimal.valueOf(match).divide(BigDecimal.valueOf(required.size()), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal computeInterestsScore(Student s, Project p, Subject sub) {
        Set<Keyword> keywords = (p != null) ? p.getKeywords() : sub.getKeywords();
        if (keywords == null || keywords.isEmpty()) return new BigDecimal("0.5");

        Set<Long> keywordIds = keywords.stream().map(Keyword::getId).collect(java.util.stream.Collectors.toSet());
        long match = s.getInterests().stream().filter(kw -> keywordIds.contains(kw.getId())).count();
        return BigDecimal.valueOf(match).divide(BigDecimal.valueOf(keywords.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal computeWorkTypeScore(Student s, Project p, Subject sub) {
        Set<WorkType> offered = (p != null) ? p.getWorkTypes() : sub.getWorkTypes();
        Set<WorkType> preferred = s.getPreferredWorkTypes();

        if (preferred == null || preferred.isEmpty() || offered == null || offered.isEmpty()) {
            return new BigDecimal("0.5");
        }

        long matchCount = offered.stream()
                .filter(preferred::contains)
                .count();

        return BigDecimal.valueOf(matchCount)
                .divide(BigDecimal.valueOf(offered.size()), 4, RoundingMode.HALF_UP);
    }
}
