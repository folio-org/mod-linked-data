package org.folio.linked.data.mapper.resource.monograph.work;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = WorkReference.class)
public class WorkReferenceMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final ResourceRepository resourceRepository;

  @Override
  public <T> T toDto(Resource source, T parentDto, Resource parentResource) {
    Consumer<WorkReference> workConsumer = work -> handleMappedWork(source, parentDto, work);
    coreMapper.mapToDtoWithEdges(source, workConsumer, WorkReference.class);
    return parentDto;
  }

  private <T> void handleMappedWork(Resource source, T destination, WorkReference work) {
    work.setId(String.valueOf(source.getResourceHash()));
    if (destination instanceof Instance instance) {
      instance.addWorkReferenceItem(work);
    }
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var work = (WorkReference) dto;
    if (nonNull(work.getId())) {
      return resourceRepository.findById(Long.parseLong(work.getId()))
        .orElseThrow(() -> new NotFoundException("Work with id [" + work.getId() + " is not found"));
    } else {
      throw new ValidationException("Work id", "null");
    }
  }

}
