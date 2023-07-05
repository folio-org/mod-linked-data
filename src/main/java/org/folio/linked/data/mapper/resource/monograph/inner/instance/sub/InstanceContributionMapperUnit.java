package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.PERSON;
import static org.folio.linked.data.util.BibframeConstants.PERSON_URL;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ROLE_URL;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CONTRIBUTION, predicate = CONTRIBUTION_PRED, dtoClass = ContributionField.class)
public class InstanceContributionMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var contribution = coreMapper.readResourceDoc(source, Contribution.class);
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);
    coreMapper.addMappedProperties(source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new ContributionField().contribution(contribution));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var contribution = ((ContributionField) dto).getContribution();
    var resource = new Resource();
    resource.setLabel(CONTRIBUTION_URL);
    resource.setType(resourceTypeService.get(CONTRIBUTION_URL));
    coreMapper.mapResourceEdges(contribution.getAgent(), resource, AGENT_PRED, this::personToEntity);
    coreMapper.mapPropertyEdges(contribution.getRole(), resource, ROLE_PRED, ROLE_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private Resource personToEntity(PersonField dto, String predicate) {
    var resource = new Resource();
    resource.setLabel(PERSON_URL);
    resource.setType(resourceTypeService.get(PERSON));
    resource.setDoc(getDoc(dto));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(PersonField dto) {
    var map = new HashMap<String, List<JsonNode>>();
    if (!dto.getPerson().getSameAs().isEmpty()) {
      final List<JsonNode> nodes = dto.getPerson().getSameAs().stream()
        .map(coreMapper::toJson)
        .collect(Collectors.toList());
      map.put(SAME_AS_PRED, nodes);
    }
    return coreMapper.toJson(map);
  }

}
