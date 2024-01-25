package org.folio.linked.data.mapper.resource.monograph.work;

import static java.util.Objects.nonNull;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import java.util.function.Consumer;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = WorkReference.class)
public class WorkReferenceMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;
  private final AgentRoleAssigner agentRoleAssigner;
  private final ResourceRepository resourceRepository;


  public WorkReferenceMapperUnit(CoreMapper coreMapper, @Lazy SubResourceMapper mapper, AgentRoleAssigner roleAssigner,
                                 ResourceRepository resourceRepository) {
    this.coreMapper = coreMapper;
    this.mapper = mapper;
    this.agentRoleAssigner = roleAssigner;
    this.resourceRepository = resourceRepository;
  }

  @Override
  public <T> T toDto(Resource source, T destination) {
    Consumer<WorkReference> workConsumer = work -> handleMappedWork(source, destination, work);
    coreMapper.mapWithResources(mapper, source, workConsumer, WorkReference.class);
    return destination;
  }

  private <T> void handleMappedWork(Resource source, T destination, WorkReference work) {
    work.setId(String.valueOf(source.getResourceHash()));
    if (work.getCreator() != null) {
      work.getCreator().forEach(creator -> agentRoleAssigner.assignRoles(creator, source));
    }
    if (work.getContributor() != null) {
      work.getContributor().forEach(contributor -> agentRoleAssigner.assignRoles(contributor, source));
    }
    if (destination instanceof Instance instance) {
      instance.addWorkReferenceItem(work);
    }
  }

  @Override
  public Resource toEntity(Object dto) {
    var work = (WorkReference) dto;
    if (nonNull(work.getId())) {
      return resourceRepository.findById(Long.parseLong(work.getId()))
        .orElseThrow(() -> new NotFoundException("Work with id [" + work.getId() + " is not found"));
    } else {
      throw new ValidationException("Work id", "null");
    }
  }

}
