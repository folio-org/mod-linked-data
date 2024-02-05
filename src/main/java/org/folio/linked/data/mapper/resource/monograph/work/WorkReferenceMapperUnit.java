package org.folio.linked.data.mapper.resource.monograph.work;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = WorkReference.class)
public class WorkReferenceMapperUnit implements SingleResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final ResourceRepository resourceRepository;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Instance instance) {
      var workReference = coreMapper.toDtoWithEdges(source, WorkReference.class, false);
      workReference.setId(String.valueOf(source.getResourceHash()));
      instance.addWorkReferenceItem(workReference);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var work = (WorkReference) dto;
    if (nonNull(work.getId())) {
      return resourceRepository.findById(Long.parseLong(work.getId()))
        .orElseThrow(() -> new NotFoundException("Work with id [" + work.getId() + "] is not found"));
    } else {
      throw new ValidationException("Work id", "null");
    }
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Collections.singleton(Instance.class);
  }
}
