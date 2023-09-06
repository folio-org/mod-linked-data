package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.Bibframe2Constants.AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_URL;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution2;
import org.folio.linked.data.domain.dto.ContributionField2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = CONTRIBUTION_URL, predicate = CONTRIBUTION_PRED, dtoClass = ContributionField2.class)
public class Instance2ContributionMapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final List<SubResourceMapperUnit<?>> mapperUnits;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var contribution = coreMapper.readResourceDoc(source, Contribution2.class);
    getResourceEdge(source).ifPresent(re -> {
      var typeUri = re.getTarget().getLastType().getTypeUri();
      var mapper = getMapperUnit(source, typeUri, destination);
      coreMapper.addMappedResources(mapper, source, AGENT_PRED, contribution);
    });
    coreMapper.addMappedProperties(source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new ContributionField2().contribution(contribution));
    return destination;
  }


  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var contribution = ((ContributionField2) dto).getContribution();
    var resource = new Resource();
    resource.setLabel(CONTRIBUTION_URL);
    resource.addType(resourceTypeService.get(CONTRIBUTION_URL));
    coreMapper.mapResourceEdges(contribution.getAgent(), resource, AGENT_PRED, Contribution2.class,
      subResourceMapper::toEntity);
    coreMapper.mapPropertyEdges(contribution.getRole(), resource, ROLE_PRED, ROLE_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private SubResourceMapperUnit<Contribution2> getMapperUnit(Resource source, String type, Instance2 destination) {
    return mapperUnits.stream()
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        return AGENT_PRED.equals(annotation.predicate()) && type.equals(annotation.type());
      })
      .findFirst()
      .map(map -> (SubResourceMapperUnit<Contribution2>) map)
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + source.getLastType().getTypeUri()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + AGENT_PRED + RIGHT_SQUARE_BRACKET + AND
        + destination.getClass().getSimpleName()));
  }

  private Optional<ResourceEdge> getResourceEdge(Resource source) {
    return source.getOutgoingEdges().stream()
      .filter(re -> AGENT_PRED.equals(re.getPredicate().getLabel()))
      .findFirst();
  }


}