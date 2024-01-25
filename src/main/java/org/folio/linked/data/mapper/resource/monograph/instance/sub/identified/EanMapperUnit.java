package org.folio.linked.data.mapper.resource.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Ean;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_EAN, predicate = MAP, dtoClass = EanField.class)
public class EanMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <T> T toDto(Resource source, T destination) {
    var ean = coreMapper.readResourceDoc(source, Ean.class);
    ean.setId(String.valueOf(source.getResourceHash()));
    if (destination instanceof Instance instance) {
      instance.addMapItem(new EanField().ean(ean));
    }
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var ean = ((EanField) dto).getEan();
    var resource = new Resource();
    resource.setLabel(getFirstValue(ean::getValue));
    resource.addType(ID_EAN);
    resource.setDoc(getDoc(ean));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Ean dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, EAN_VALUE, dto.getValue());
    putProperty(map, QUALIFIER, dto.getQualifier());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
