package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.EXPRESSION_OF;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_TO;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;

import java.util.Set;
import org.folio.linked.data.domain.dto.HubReferenceWithType;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.ReferenceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = HUB, predicate = {EXPRESSION_OF, RELATED_TO}, requestDto = Reference.class)
public class HubReferenceMapperUnit extends ReferenceMapperUnit {

  public HubReferenceMapperUnit(ReferenceService referenceService) {
    super(referenceService);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto) {
      var reference = toReference(resourceToConvert);
      getPropertyValues(resourceToConvert, LINK).stream()
        .findFirst()
        .ifPresent(reference::setRdfLink);
      var referenceWithType = new HubReferenceWithType()
        .hub(reference)
        .relation(context.predicate().getUri());
      workDto.addHubsItem(referenceWithType);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class);
  }
}
