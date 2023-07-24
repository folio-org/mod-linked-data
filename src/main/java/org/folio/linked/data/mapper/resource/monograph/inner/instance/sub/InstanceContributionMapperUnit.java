package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ROLE_URL;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.Instance;
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
@MapperUnit(type = CONTRIBUTION, predicate = CONTRIBUTION_PRED, dtoClass = ContributionField.class)
public class InstanceContributionMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final List<SubResourceMapperUnit<?>> mapperUnits;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var contribution = coreMapper.readResourceDoc(source, Contribution.class);
    getResouceEdge(source).ifPresent(re -> {
      var label = re.getTarget().getType().getSimpleLabel();
      var mapper = getMapperUnit(source, label, destination);
      coreMapper.addMappedResources(mapper, source, AGENT_PRED, contribution);
    });
    coreMapper.addMappedProperties(source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new ContributionField().contribution(contribution));
    return destination;
  }


  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var contribution = ((ContributionField) dto).getContribution();
    var resource = new Resource();
    resource.setLabel(CONTRIBUTION_URL);
    resource.setType(resourceTypeService.get(CONTRIBUTION_URL));
    coreMapper.mapResourceEdges(contribution.getAgent(), resource, AGENT_PRED, Contribution.class,
      subResourceMapper::toEntity);
    coreMapper.mapPropertyEdges(contribution.getRole(), resource, ROLE_PRED, ROLE_URL);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private SubResourceMapperUnit<Contribution> getMapperUnit(Resource source, String label, Instance destination) {
    return mapperUnits.stream()
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        return AGENT_PRED.equals(annotation.predicate()) && label.equals(annotation.type());
      })
      .findFirst()
      .map(map -> (SubResourceMapperUnit<Contribution>) map)
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + source.getType().getSimpleLabel()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + AGENT_PRED + RIGHT_SQUARE_BRACKET + AND
        + destination.getClass().getSimpleName()));
  }

  private Optional<ResourceEdge> getResouceEdge(Resource source) {
    return source.getOutgoingEdges().stream()
      .filter(re -> AGENT_PRED.equals(re.getPredicate().getLabel()))
      .findFirst();
  }


}
