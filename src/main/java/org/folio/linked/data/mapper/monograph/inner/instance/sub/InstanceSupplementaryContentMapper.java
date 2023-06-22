package org.folio.linked.data.mapper.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.SUPPLEMENTARY_CONTENT_PRED;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(SUPPLEMENTARY_CONTENT_PRED)
public class InstanceSupplementaryContentMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var property = toProperty(objectMapper, source);
    destination.addSupplementaryContentItem(property);
    return destination;
  }
}
