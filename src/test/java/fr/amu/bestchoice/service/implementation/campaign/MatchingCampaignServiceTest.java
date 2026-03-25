package fr.amu.bestchoice.service.implementation.campaign;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignRequest;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingCampaignMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingCampaignServiceTest {

    @Mock
    private MatchingCampaignRepository campaignRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private MatchingCampaignMapper mapper;

    @InjectMocks
    private MatchingCampaignService campaignService;

    private Teacher teacher;
    private MatchingCampaignRequest projectRequest;
    private MatchingCampaign campaign;

    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        teacher.setId(1L);

        projectRequest = new MatchingCampaignRequest(
                "Campagne Test",
                "Description",
                "2024-2025",
                1,
                MatchingCampaignType.PROJECT,
                MatchingAlgorithmType.WEIGHTED,
                java.math.BigDecimal.valueOf(0.5),
                java.math.BigDecimal.valueOf(0.3),
                java.math.BigDecimal.valueOf(0.2),
                1L,
                List.of(2L),
                List.of(3L),
                null
        );

        campaign = new MatchingCampaign();
        campaign.setId(1L);
        campaign.setStudents(new HashSet<>());
        campaign.setProjects(new HashSet<>());
        campaign.setSubjects(new HashSet<>());
    }

    @Test
    void create_ShouldCreateProjectCampaign_WhenRequestIsValid() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(mapper.toEntity(projectRequest)).thenReturn(campaign);
        when(studentRepository.findAllById(any())).thenReturn(Collections.singletonList(new Student()));
        when(projectRepository.findAllById(any())).thenReturn(Collections.singletonList(new Project()));
        when(campaignRepository.save(any())).thenReturn(campaign);
        when(mapper.toResponse(campaign)).thenReturn(mock(MatchingCampaignResponse.class));

        MatchingCampaignResponse response = campaignService.create(projectRequest);

        assertThat(response).isNotNull();
        verify(campaignRepository).save(campaign);
        verify(projectRepository).findAllById(any());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenTeacherNotFound() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.create(projectRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_ShouldReturnResponse_WhenExists() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(mapper.toResponse(campaign)).thenReturn(mock(MatchingCampaignResponse.class));

        MatchingCampaignResponse response = campaignService.findById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenNotExists() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campaignService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_ShouldCallRepository() {
        campaignService.delete(1L);
        verify(campaignRepository).deleteById(1L);
    }
}
