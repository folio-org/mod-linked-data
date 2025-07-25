package org.folio.linked.data.mapper.dto.monograph.work;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.mapper.dto.monograph.work.WorkMapperUnit.SUPPORTED_NOTES;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.NoteMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES, requestDto = IdField.class)
public class WorkReferenceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class
  );

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final ResourceRepository resourceRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var workResponse = coreMapper.toDtoWithEdges(resourceToConvert, WorkResponse.class, false);
      workResponse.setId(String.valueOf(resourceToConvert.getId()));
      ofNullable(resourceToConvert.getDoc())
        .ifPresent(doc -> workResponse.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      instance.addWorkReferenceItem(workResponse);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var workIdField = (IdField) dto;
    if (nonNull(workIdField.getId())) {
      return resourceRepository.findById(Long.parseLong(workIdField.getId()))
        .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Work", workIdField.getId()));
    } else {
      throw exceptionBuilder.requiredException("Work id");
    }
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
