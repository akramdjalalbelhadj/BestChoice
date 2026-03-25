package fr.amu.bestchoice.service.implementation.preference;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.StudentPreferenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {

    @Mock
    private StudentPreferenceRepository preferenceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private MatchingCampaignRepository campaignRepository;
    @Mock
    private StudentPreferenceMapper preferenceMapper;

    @InjectMocks
    private PreferenceService preferenceService;

    private Student student;
    private Project project;
    private StudentPreference preference;
    private PreferenceCreateRequest createRequest;
    private PreferenceResponse preferenceResponse;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("student@test.com");

        student = new Student();
        student.setId(1L);
        student.setUser(user);

        project = new Project();
        project.setId(1L);
        project.setActive(true);
        project.setComplet(false);
        project.setAssignedStudents(new ArrayList<>());

        preference = new StudentPreference();
        preference.setId(1L);
        preference.setStudent(student);
        preference.setProject(project);
        preference.setRank(1);
        preference.setStatus(PreferenceStatus.PENDING);

        createRequest = new PreferenceCreateRequest(1L, 1L, 1L, null, 1, "Motivation", "Comment");
        preferenceResponse = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.PENDING, LocalDateTime.now());
    }

    @Test
    void create_ShouldReturnPreferenceResponse_WhenValidRequest() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(preferenceRepository.existsByStudentIdAndProjectId(1L, 1L)).thenReturn(false);
        when(preferenceRepository.existsByStudentIdAndRank(1L, 1)).thenReturn(false);
        when(preferenceMapper.toEntity(createRequest)).thenReturn(preference);
        when(preferenceRepository.save(any(StudentPreference.class))).thenReturn(preference);
        when(preferenceMapper.toResponse(any())).thenReturn(preferenceResponse);

        // When
        PreferenceResponse result = preferenceService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(preferenceRepository).save(any(StudentPreference.class));
    }

    @Test
    void create_ShouldThrowBusinessException_WhenRankAlreadyUsed() {
        // Given
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(preferenceRepository.existsByStudentIdAndRank(1L, 1)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> preferenceService.create(createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void findByStudentId_ShouldReturnList() {
        // Given
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(preferenceRepository.findByStudentIdOrderByRankAsc(1L)).thenReturn(List.of(preference));
        when(preferenceMapper.toResponseList(anyList())).thenReturn(List.of(preferenceResponse));

        // When
        List<PreferenceResponse> result = preferenceService.findByStudentId(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void delete_ShouldCallRepository() {
        // Given
        when(preferenceRepository.findById(1L)).thenReturn(Optional.of(preference));

        // When
        preferenceService.delete(1L);

        // Then
        verify(preferenceRepository).delete(preference);
    }

    @Test
    void changeStatus_ShouldUpdateStatus() {
        // Given
        when(preferenceRepository.findById(1L)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(StudentPreference.class))).thenReturn(preference);
        when(preferenceMapper.toResponse(any())).thenReturn(preferenceResponse);

        // When
        PreferenceResponse result = preferenceService.changeStatus(1L, PreferenceStatus.ACCEPTED);

        // Then
        assertThat(preference.getStatus()).isEqualTo(PreferenceStatus.ACCEPTED);
        verify(preferenceRepository).save(preference);
    }
}
