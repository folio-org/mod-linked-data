package org.folio.linked.data.mapper.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_RETENTION;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.RetentionPolicy;
import org.folio.linked.data.domain.dto.RetentionPolicyField;
import org.folio.linked.data.mapper.monograph.inner.item.sub.ItemSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(ITEM_RETENTION)
public class RetentionMapper implements ItemSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var policy = readResourceDoc(mapper, source, RetentionPolicy.class);
    destination.addUsageAndAccessPolicyItem(new RetentionPolicyField().retentionPolicy(policy));
    return destination;
  }
}
