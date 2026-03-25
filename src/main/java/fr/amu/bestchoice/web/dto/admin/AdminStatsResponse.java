package fr.amu.bestchoice.web.dto.admin;

import java.util.List;
import java.util.Map;

public record AdminStatsResponse(

        // ── Projets ──────────────────────────────────────────────────────────
        long totalProjects,
        long activeProjects,
        long inactiveProjects,
        long completedProjects,
        long totalProjectCapacity,
        Map<String, Long> projectsByWorkType,
        Map<String, Long> projectsBySemester,
        List<NameCountEntry> topTeachersByProjects,

        // ── Options (Subjects) ────────────────────────────────────────────────
        long totalSubjects,
        long activeSubjects,
        long inactiveSubjects,
        long totalSubjectCapacity,
        Map<String, Long> subjectsByWorkType,
        Map<String, Long> subjectsBySemester,
        List<NameCountEntry> topTeachersBySubjects,

        // ── Utilisateurs ─────────────────────────────────────────────────────
        long totalTeachers,
        long totalStudents

) {
    public record NameCountEntry(String name, long count) {}
}
