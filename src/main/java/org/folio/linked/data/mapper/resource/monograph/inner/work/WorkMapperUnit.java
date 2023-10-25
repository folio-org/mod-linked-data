package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.JsonNode;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.dictionary.ResourceTypeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = Work.class)
public class WorkMapperUnit implements InstanceSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;
  private final AgentRoleAssigner agentRoleAssigner;

  private final ResourceTypeService resourceTypeService;


  public WorkMapperUnit(CoreMapper coreMapper, @Lazy SubResourceMapper mapper, AgentRoleAssigner roleAssigner,
                        ResourceTypeService resourceTypeService) {
    this.coreMapper = coreMapper;
    this.mapper = mapper;
    this.agentRoleAssigner = roleAssigner;
    this.resourceTypeService = resourceTypeService;
  }

  @Override
  public Instance toDto(Resource source, Instance destination) {
    Consumer<Work> workConsumer = work -> handleMappedWork(source, destination, work);
    coreMapper.mapWithResources(mapper, source, workConsumer, Work.class);
    return destination;
  }

  private void handleMappedWork(Resource source, Instance destination, Work work) {
    work.setId(String.valueOf(source.getResourceHash()));
    if (work.getCreator() != null) {
      work.getCreator().forEach(creator -> agentRoleAssigner.assignRoles(creator, source));
    }
    if (work.getContributor() != null) {
      work.getContributor().forEach(contributor -> agentRoleAssigner.assignRoles(contributor, source));
    }
    destination.addInstantiatesItem(work);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var work = (Work) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(work::getResponsibiltyStatement));
    resource.addType(resourceTypeService.get(WORK));
    resource.setDoc(toDoc(work));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode toDoc(Work work) {
    var map = new HashMap<String, List<String>>();
    map.put(RESPONSIBILITY_STATEMENT.getValue(), work.getResponsibiltyStatement());
    map.put(TARGET_AUDIENCE.getValue(), work.getTargetAudience());
    map.put(LANGUAGE.getValue(), work.getLanguage());
    map.put(SUMMARY.getValue(), work.getSummary());
    map.put(TABLE_OF_CONTENTS.getValue(), work.getTableOfContents());
    return coreMapper.toJson(map);
  }
}
