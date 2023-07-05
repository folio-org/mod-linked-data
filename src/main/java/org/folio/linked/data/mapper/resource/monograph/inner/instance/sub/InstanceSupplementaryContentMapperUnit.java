package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = SUPP_CONTENT_PRED, dtoClass = Property.class)
public class InstanceSupplementaryContentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var property = coreMapper.toProperty(source);
    destination.addSupplementaryContentItem(property);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    return coreMapper.propertyToEntity((Property) dto, SUPP_CONTENT_URL);
  }
}
