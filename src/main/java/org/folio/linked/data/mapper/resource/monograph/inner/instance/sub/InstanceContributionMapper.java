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
import static org.folio.linked.data.util.MappingUtil.addMappedPersonLookups;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.hash;
import static org.folio.linked.data.util.MappingUtil.mapPropertyEdges;
import static org.folio.linked.data.util.MappingUtil.mapResourceEdges;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;
import static org.folio.linked.data.util.MappingUtil.toJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = CONTRIBUTION, predicate = CONTRIBUTION_PRED, dtoClass = ContributionField.class)
public class InstanceContributionMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final ObjectMapper mapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var contribution = readResourceDoc(mapper, source, Contribution.class);
    addMappedPersonLookups(mapper, source, AGENT_PRED, contribution::addAgentItem);
    addMappedProperties(mapper, source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new ContributionField().contribution(contribution));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var contribution = ((ContributionField) dto).getContribution();
    var resource = new Resource();
    resource.setLabel(CONTRIBUTION_URL);
    resource.setType(resourceTypeService.get(CONTRIBUTION_URL));
    mapResourceEdges(contribution.getAgent(), resource,
      () -> predicateService.get(AGENT_PRED), this::personToEntity);
    mapPropertyEdges(contribution.getRole(), resource, () -> predicateService.get(ROLE_PRED),
      () -> resourceTypeService.get(ROLE_URL), mapper);
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  private Resource personToEntity(PersonField dto, String predicate) {
    var resource = new Resource();
    resource.setLabel(PERSON_URL);
    resource.setType(resourceTypeService.get(PERSON));
    resource.setDoc(toJson(getDoc(dto), mapper));
    resource.setResourceHash(hash(resource, mapper));
    return resource;
  }

  private Map<String, List<JsonNode>> getDoc(PersonField dto) {
    var map = new HashMap<String, List<JsonNode>>();
    if (!dto.getPerson().getSameAs().isEmpty()) {
      final List<JsonNode> nodes = dto.getPerson().getSameAs().stream()
        .map(l -> toJson(l, mapper))
        .collect(Collectors.toList());
      map.put(SAME_AS_PRED, nodes);
    }
    return map;
  }

}
