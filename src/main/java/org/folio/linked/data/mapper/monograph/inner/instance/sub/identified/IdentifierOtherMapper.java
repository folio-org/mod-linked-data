package org.folio.linked.data.mapper.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.IdentifierOther;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.OtherIdentifierField;
import org.folio.linked.data.mapper.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(IDENTIFIERS_OTHER)
public class IdentifierOtherMapper implements InstanceSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var identifier = readResourceDoc(objectMapper, source, IdentifierOther.class);
    destination.addIdentifiedByItem(new OtherIdentifierField().identifier(identifier));
    return destination;
  }

}
