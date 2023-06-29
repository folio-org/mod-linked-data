package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER_URL;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierOther;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.OtherIdentifierField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = IDENTIFIERS_OTHER, predicate = IDENTIFIED_BY_PRED, dtoClass = OtherIdentifierField.class)
public class IdentifierOtherMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var identifier = commonMapper.readResourceDoc(source, IdentifierOther.class);
    destination.addIdentifiedByItem(new OtherIdentifierField().identifier(identifier));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var other = ((OtherIdentifierField) dto).getIdentifier();
    var resource = new Resource();
    resource.setLabel(IDENTIFIERS_OTHER_URL);
    resource.setType(resourceTypeService.get(IDENTIFIERS_OTHER));
    resource.setDoc(getDoc(other));
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(IdentifierOther dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_URL, dto.getValue());
    map.put(QUALIFIER_URL, dto.getQualifier());
    return commonMapper.toJson(map);
  }

}
