package org.folio.linked.data.mapper.resource.monograph.inner.common.contribution.agent;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION_URL;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Agent2;
import org.folio.linked.data.domain.dto.Contribution2;
import org.folio.linked.data.domain.dto.Lookup2;
import org.folio.linked.data.domain.dto.OrganizationField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.contribution.ContributionSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ORGANIZATION, predicate = AGENT_PRED, dtoClass = OrganizationField2.class)
public class OrganizationMapperUnit implements ContributionSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Contribution2 toDto(Resource source, Contribution2 destination) {
    var agent = coreMapper.readResourceDoc(source, Agent2.class);
    destination.addAgentItem(new OrganizationField2().organization(agent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var agent = ((OrganizationField2) dto).getOrganization();
    var resource = new Resource();
    resource.setLabel(ORGANIZATION_URL);
    resource.setType(resourceTypeService.get(ORGANIZATION));
    resource.setDoc(getDoc(agent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Agent2 dto) {
    var map = new HashMap<String, List<Lookup2>>();
    map.put(SAME_AS_PRED, dto.getSameAs());
    return coreMapper.toJson(map);
  }

}
