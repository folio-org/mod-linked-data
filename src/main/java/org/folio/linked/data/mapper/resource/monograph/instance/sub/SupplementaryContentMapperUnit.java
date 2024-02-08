package org.folio.linked.data.mapper.resource.monograph.instance.sub;

import static org.folio.ld.dictionary.ResourceTypeDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.SupplementaryContent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = SUPPLEMENTARY_CONTENT, predicate = PredicateDictionary.SUPPLEMENTARY_CONTENT,
  dtoClass = SupplementaryContent.class)
public class SupplementaryContentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Instance instance) {
      var supplementaryContent = coreMapper.toDtoWithEdges(source, SupplementaryContent.class, false);
      supplementaryContent.setId(String.valueOf(source.getResourceHash()));
      instance.addSupplementaryContentItem(supplementaryContent);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var supplementaryContent = (SupplementaryContent) dto;
    var resource = new Resource()
      .setLabel(getFirstValue(supplementaryContent::getName))
      .addType(SUPPLEMENTARY_CONTENT)
      .setDoc(getDoc(supplementaryContent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(SupplementaryContent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PropertyDictionary.LINK, dto.getLink());
    putProperty(map, PropertyDictionary.NAME, dto.getName());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
