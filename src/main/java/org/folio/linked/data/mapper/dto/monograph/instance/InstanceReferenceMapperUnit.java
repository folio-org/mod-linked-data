package org.folio.linked.data.mapper.dto.monograph.instance;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.mapper.dto.monograph.instance.InstanceMapperUnit.SUPPORTED_NOTES;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdField;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
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
@MapperUnit(type = INSTANCE, predicate = INSTANTIATES, requestDto = IdField.class)
public class InstanceReferenceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    WorkRequest.class,
    WorkResponse.class
  );

  private final CoreMapper coreMapper;
  private final NoteMapper noteMapper;
  private final ResourceRepository resourceRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof WorkResponse work) {
      var instanceResponse = coreMapper.toDtoWithEdges(source, InstanceResponse.class, false);
      instanceResponse.setId(String.valueOf(source.getId()));
      ofNullable(source.getDoc())
        .ifPresent(doc -> instanceResponse.setNotes(noteMapper.toNotes(doc, SUPPORTED_NOTES)));
      work.addInstanceReferenceItem(instanceResponse);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var instanceIdField = (IdField) dto;
    if (nonNull(instanceIdField.getId())) {
      return resourceRepository.findById(Long.parseLong(instanceIdField.getId()))
        .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Instance", instanceIdField.getId()));
    } else {
      throw exceptionBuilder.requiredException("Instance id");
    }
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

}
