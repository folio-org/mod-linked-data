package org.folio.linked.data.mapper.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierLocal;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
import org.folio.linked.data.mapper.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(IDENTIFIERS_LOCAL)
public class LocalMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var local = readResourceDoc(objectMapper, source, IdentifierLocal.class);
    addMappedProperties(objectMapper, source, ASSIGNER_PRED, local::addAssignerItem);
    destination.addIdentifiedByItem(new LocalIdentifierField().local(local));
    return destination;
  }

}
