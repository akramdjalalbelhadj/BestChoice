package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    @Mapping(target = "teacher", ignore = true)
    Subject toEntity(SubjectCreateRequest request);

    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName",
            expression = "java(entity.getTeacher().getUser().getFirstName() + \" \" + entity.getTeacher().getUser().getLastName())")
    SubjectResponse toResponse(Subject entity);

    List<SubjectResponse> toResponseList(List<Subject> entities);
}