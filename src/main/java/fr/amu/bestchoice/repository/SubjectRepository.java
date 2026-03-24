package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.model.enums.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des matières optionnelles (subjects).
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByTeacherId(Long teacherId);

    List<Subject> findByActiveTrue();

    List<Subject> findByWorkTypesContaining(WorkType workType);

    @Query("SELECT s FROM Subject s WHERE s.active = true AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Subject> searchActiveSubjects(@Param("query") String query);
}