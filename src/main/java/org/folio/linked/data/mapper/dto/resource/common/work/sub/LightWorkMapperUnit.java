package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_EDITION;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_VERSION;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_WORK;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.linked.data.util.ResourceUtils.getFirstPropertyValue;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.LightWork;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = LIGHT_RESOURCE, predicate = {IS_PART_OF, OTHER_EDITION, OTHER_VERSION, RELATED_WORK},
  requestDto = LightWork.class)
public class LightWorkMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    WorkResponse.class
  );
  private final CoreMapper coreMapper;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (resourceToConvert.isOfType(SERIES)) {
      return null;
    }
    if (parentDto instanceof WorkResponse workResponse) {
      var lightWork = coreMapper.toDtoWithEdges(resourceToConvert, LightWork.class, false);
      lightWork.setId(String.valueOf(resourceToConvert.getId()));
      lightWork.setLabel(constructUiLabel(resourceToConvert));
      lightWork.setRelation(context.predicate().getUri());
      workResponse.addAnalyticalEntryItem(lightWork);
    }
    return parentDto;
  }

  private static String constructUiLabel(Resource lightWork) {
    var workLabel = getFirstPropertyValue(lightWork, LABEL);
    return lightWork.getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getUri().equals(CREATOR.getUri()))
      .map(ResourceEdge::getTarget)
      .map(creator -> getFirstPropertyValue(creator, LABEL))
      .findFirst()
      .map(creatorLabel -> workLabel + ". " + creatorLabel)
      .orElse(workLabel);
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    throw exceptionBuilder.notSupportedException(LIGHT_RESOURCE.name(), "Create or update");
  }

}
