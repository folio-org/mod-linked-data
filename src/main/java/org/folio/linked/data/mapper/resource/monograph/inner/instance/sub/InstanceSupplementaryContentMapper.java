package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPP_CONTENT_URL;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = SUPP_CONTENT_PRED, dtoClass = Property.class)
public class InstanceSupplementaryContentMapper implements InstanceSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var property = commonMapper.toProperty(source);
    destination.addSupplementaryContentItem(property);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    return commonMapper.propertyToEntity((Property) dto, SUPP_CONTENT_URL);
  }
}