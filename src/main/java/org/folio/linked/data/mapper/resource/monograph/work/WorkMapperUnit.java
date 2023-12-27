package org.folio.linked.data.mapper.resource.monograph.work;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = WORK, predicate = INSTANTIATES, dtoClass = Work.class)
public class WorkMapperUnit implements InstanceSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final SubResourceMapper mapper;
  private final AgentRoleAssigner agentRoleAssigner;


  public WorkMapperUnit(CoreMapper coreMapper, @Lazy SubResourceMapper mapper, AgentRoleAssigner roleAssigner) {
    this.coreMapper = coreMapper;
    this.mapper = mapper;
    this.agentRoleAssigner = roleAssigner;
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
  public Resource toEntity(Object dto) {
    var work = (Work) dto;
    var resource = new Resource();
    resource.addType(WORK);
    resource.setDoc(getDoc(work));
    coreMapper.mapTopEdges(work.getClassification(), resource, CLASSIFICATION, Work.class, mapper::toEntity);
    coreMapper.mapTopEdges(work.getContent(), resource, CONTENT, Work.class, mapper::toEntity);
    mapContributionEdges(work.getCreator(), resource, CREATOR);
    mapContributionEdges(work.getContributor(), resource, CONTRIBUTOR);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private void mapContributionEdges(List<AgentTypeInner> dtoList, @NonNull Resource source,
                                    @NonNull Predicate predicate) {
    if (nonNull(dtoList)) {
      for (var dto : dtoList) {
        var resource = mapper.toEntity(dto, predicate, Work.class);
        if (nonNull(resource.getDoc()) || isNotEmpty(resource.getOutgoingEdges())) {
          source.getOutgoingEdges().add(new ResourceEdge(source, resource, predicate));
          Optional.ofNullable(agentRoleAssigner.getAgent(dto).getRoles())
            .ifPresent(roles -> roles.forEach(role ->
              source.getOutgoingEdges().add(new ResourceEdge(source, resource, getPredicate(role)))));
        }
      }
    }
  }

  private Predicate getPredicate(String role) {
    return Arrays.stream(PredicateDictionary.values())
      .filter(predicate -> predicate.getUri().equals(role))
      .findFirst()
      .orElseThrow();
  }

  private JsonNode getDoc(Work dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, RESPONSIBILITY_STATEMENT, dto.getResponsibiltyStatement());
    putProperty(map, TARGET_AUDIENCE, dto.getTargetAudience());
    putProperty(map, LANGUAGE, dto.getLanguage());
    putProperty(map, SUMMARY, dto.getSummary());
    putProperty(map, TABLE_OF_CONTENTS, dto.getTableOfContents());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
