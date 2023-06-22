package org.folio.linked.data.mapper.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Ean;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(IDENTIFIERS_EAN)
public class EanMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var ean = readResourceDoc(objectMapper, source, Ean.class);
    destination.addIdentifiedByItem(new EanField().ean(ean));
    return destination;
  }

}
