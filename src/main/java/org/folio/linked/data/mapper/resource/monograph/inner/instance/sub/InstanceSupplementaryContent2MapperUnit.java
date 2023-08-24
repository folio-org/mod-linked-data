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
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.SupplementaryContent2;
import org.folio.linked.data.domain.dto.SupplementaryContentField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = SUPP_CONTENT, predicate = SUPP_CONTENT_PRED, dtoClass = SupplementaryContentField2.class)
public class InstanceSupplementaryContent2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var supplementaryContent = coreMapper.readResourceDoc(source, SupplementaryContent2.class);
    destination.addSupplementaryContentItem(
      new SupplementaryContentField2().supplementaryContent(supplementaryContent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var supplementaryContent = ((SupplementaryContentField2) dto).getSupplementaryContent();
    var resource = new Resource();
    resource.setLabel(SUPP_CONTENT_URL);
    resource.setType(resourceTypeService.get(SUPP_CONTENT));
    resource.setDoc(getDoc(supplementaryContent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(SupplementaryContent2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, dto.getLabel());
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
