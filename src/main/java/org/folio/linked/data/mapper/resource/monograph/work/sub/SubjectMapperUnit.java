package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Subject;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CONCEPT, predicate = SUBJECT, dtoClass = Subject.class)
public class SubjectMapperUnit implements WorkSubResourceMapperUnit {

  private final ResourceRepository resourceRepository;

  @Override
  public <T> T toDto(Resource source, T parentDto, Resource parentResource) {
    var subject = new Subject()
      .id(source.getResourceHash().toString())
      .label(source.getLabel());
    if (parentDto instanceof Work work) {
      work.addSubjectsItem(subject);
    }
    if (parentDto instanceof WorkReference work) {
      work.addSubjectsItem(subject);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var subject = (Subject) dto;
    return resourceRepository
      .findById(Long.parseLong(subject.getId()))
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + subject.getId() + IS_NOT_FOUND));
  }
}
