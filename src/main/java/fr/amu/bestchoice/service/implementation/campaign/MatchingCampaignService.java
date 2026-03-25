package fr.amu.bestchoice.service.implementation.campaign;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.model.entity.MatchingCampaignType;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.service.interfaces.IMatchingCampaignService;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignRequest;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingCampaignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchingCampaignService implements IMatchingCampaignService
{

    private final MatchingCampaignRepository campaignRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final SubjectRepository subjectRepository;
    private final MatchingCampaignMapper mapper;

    @Override
    @Transactional
    public MatchingCampaignResponse create(MatchingCampaignRequest request) {
        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new NotFoundException("Enseignant introuvable"));

        MatchingCampaign campaign = mapper.toEntity(request);
        campaign.setTeacher(teacher);

        if (request.studentIds() != null && !request.studentIds().isEmpty()) {
            campaign.getStudents().addAll(studentRepository.findAllById(request.studentIds()));
        }

        if (request.campaignType() == MatchingCampaignType.PROJECT
                && request.projectIds() != null && !request.projectIds().isEmpty()) {
            campaign.getProjects().addAll(projectRepository.findAllById(request.projectIds()));
        } else if (request.campaignType() == MatchingCampaignType.SUBJECT
                && request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            campaign.getSubjects().addAll(subjectRepository.findAllById(request.subjectIds()));
        }

        MatchingCampaign saved = campaignRepository.save(campaign);
        return mapper.toResponse(saved);
    }

    @Override
    public void addStudentsToCampaign(Long campaignId, List<Long> studentIds) {
        MatchingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campagne introuvable"));

        List<Student> students = studentRepository.findAllById(studentIds);
        campaign.getStudents().addAll(students);
        campaignRepository.save(campaign);
    }

    @Override
    public void addItemsToCampaign(Long campaignId, List<Long> itemIds) {
        MatchingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campagne introuvable"));

        if (campaign.getCampaignType() == MatchingCampaignType.PROJECT) {
            campaign.getProjects().addAll(projectRepository.findAllById(itemIds));
        } else {
            campaign.getSubjects().addAll(subjectRepository.findAllById(itemIds));
        }
        campaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchingCampaignResponse findById(Long id) {
        return campaignRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Campagne introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingCampaignResponse> findByTeacherId(Long teacherId) {
        return campaignRepository.findByTeacherId(teacherId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchingCampaignResponse> findByStudentId(Long studentId) {
        return campaignRepository.findAllByStudentIdInTable(studentId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        campaignRepository.deleteById(id);
    }
}