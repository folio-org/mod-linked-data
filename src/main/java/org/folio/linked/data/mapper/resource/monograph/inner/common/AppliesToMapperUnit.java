package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AppliesTo;
import org.folio.linked.data.domain.dto.AppliesToField;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = APPLIES_TO, predicate = APPLIES_TO_PRED, dtoClass = AppliesToField.class)
public class AppliesToMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS = Set.of(Extent.class);
  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public T toDto(Resource source, T destination) {
    var appliesTo = coreMapper.readResourceDoc(source, AppliesTo.class);
    var appliesToField = new AppliesToField().appliesTo(appliesTo);
    if (destination instanceof Extent extent) {
      extent.addAppliesToItem(appliesToField);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + destination.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + APPLIES_TO_PRED + RIGHT_SQUARE_BRACKET);
    }
    return destination;
  }

  @Override
  public Set<Class> getParentDto() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var appliesTo = ((AppliesToField) dto).getAppliesTo();
    var resource = new Resource();
    resource.setLabel(APPLIES_TO_URL);
    resource.setType(resourceTypeService.get(APPLIES_TO));
    resource.setDoc(getDoc(appliesTo));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(AppliesTo appliesTo) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, appliesTo.getLabel());
    return coreMapper.toJson(map);
  }
}
