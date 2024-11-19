package org.folio.linked.data.mapper.dto.monograph.instance;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.Set;
import org.folio.linked.data.domain.dto.IdField;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = INSTANCE, predicate = INSTANTIATES, requestDto = IdField.class)
public class InstanceReferenceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    WorkRequest.class,
    WorkResponse.class
  );

  private final CoreMapper coreMapper;
  private final ResourceRepository resourceRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  public InstanceReferenceMapperUnit(CoreMapper coreMapper,
                                     ResourceRepository resourceRepository,
                                     RequestProcessingExceptionBuilder exceptionBuilder) {
    this.coreMapper = coreMapper;
    this.resourceRepository = resourceRepository;
    this.exceptionBuilder = exceptionBuilder;
  }

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof WorkResponse work) {
      var instanceResponse = coreMapper.toDtoWithEdges(source, InstanceResponse.class, false);
      instanceResponse.setId(String.valueOf(source.getId()));
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
