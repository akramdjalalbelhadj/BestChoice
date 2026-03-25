package fr.amu.bestchoice.service.implementation.subject;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.SubjectRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SubjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private SubjectService subjectService;

    private Teacher teacher;
    private User user;
    private Subject subject;
    private SubjectCreateRequest createRequest;
    private SubjectResponse subjectResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);

        subject = new Subject();
        subject.setId(1L);
        subject.setTitle("Subject Title");
        subject.setTeacher(teacher);
        subject.setRequiredSkills(new HashSet<>());
        subject.setKeywords(new HashSet<>());

        createRequest = new SubjectCreateRequest(
                "Subject Title", "Description", "Objectives",
                Collections.emptySet(), 1, 1, 6, 1, "2024-2025",
                Collections.emptySet(), Collections.emptySet()
        );

        subjectResponse = new SubjectResponse(
                1L, "Subject Title", "Description", "Objectives",
                Collections.emptySet(), 1, 1, 6, 1, "2024-2025",
                true, 1L, "John Doe", Collections.emptySet(), Collections.emptySet()
        );
    }

    @Test
    void create_ShouldReturnResponse_WhenTeacherExists() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(subjectMapper.toEntity(createRequest)).thenReturn(subject);
        when(subjectRepository.save(any())).thenReturn(subject);
        when(subjectMapper.toResponse(subject)).thenReturn(subjectResponse);

        SubjectResponse result = subjectService.create(1L, createRequest);

        assertThat(result).isNotNull();
        verify(subjectRepository).save(any());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenTeacherDoesNotExist() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subjectService.create(1L, createRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_ShouldReturnResponse_WhenExists() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(subjectMapper.toResponse(subject)).thenReturn(subjectResponse);

        SubjectResponse result = subjectService.findById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void findAll_ShouldReturnPage() {
        Page<Subject> page = new PageImpl<>(Collections.singletonList(subject));
        when(subjectRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(subjectMapper.toResponse(subject)).thenReturn(subjectResponse);

        Page<SubjectResponse> result = subjectService.findAll(0, 10, "title", "ASC");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void activate_ShouldSetSubjectActive() {
        subject.setActive(false);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        subjectService.activate(1L);

        assertThat(subject.getActive()).isTrue();
        // Repository save is not called, we rely on dirty checking in transaction
    }

    @Test
    void deactivate_ShouldSetSubjectInactive() {
        subject.setActive(true);
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        subjectService.deactivate(1L);

        assertThat(subject.getActive()).isFalse();
        // Repository save is not called, we rely on dirty checking in transaction
    }
}
