package fr.amu.bestchoice.service.implementation.subject;

import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.repository.SubjectRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectMapper mapper;

    public SubjectResponse create(Long teacherId, SubjectCreateRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("Enseignant introuvable"));

        Subject subject = mapper.toEntity(request);
        subject.setTeacher(teacher);

        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByTeacherId(Long teacherId) {
        return mapper.toResponseList(subjectRepository.findByTeacherId(teacherId));
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return subjectRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Matière introuvable"));
    }

    public void activate(Long id) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setActive(true);
    }

    public void deactivate(Long id) {
        Subject s = subjectRepository.findById(id).orElseThrow();
        s.setActive(false);
    }
}
