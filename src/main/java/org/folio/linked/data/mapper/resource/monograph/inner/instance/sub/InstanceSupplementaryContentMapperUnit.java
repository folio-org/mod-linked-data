package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.SupplementaryContent;
import org.folio.linked.data.domain.dto.SupplementaryContentField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = SUPP_CONTENT, predicate = SUPP_CONTENT_PRED, dtoClass = SupplementaryContentField.class)
public class InstanceSupplementaryContentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var supplementaryContent = coreMapper.readResourceDoc(source, SupplementaryContent.class);
    destination.addSupplementaryContentItem(new SupplementaryContentField().supplementaryContent(supplementaryContent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var supplementaryContent = ((SupplementaryContentField) dto).getSupplementaryContent();
    var resource = new Resource();
    resource.setLabel(SUPP_CONTENT_URL);
    resource.setType(resourceTypeService.get(SUPP_CONTENT));
    resource.setDoc(getDoc(supplementaryContent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(SupplementaryContent dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, dto.getLabel());
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
