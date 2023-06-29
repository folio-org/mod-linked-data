package org.folio.linked.data.mapper.resource.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_RETENTION;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.RetentionPolicy;
import org.folio.linked.data.domain.dto.RetentionPolicyField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.item.sub.ItemSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = ITEM_RETENTION)
public class RetentionMapper implements ItemSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var policy = commonMapper.readResourceDoc(source, RetentionPolicy.class);
    destination.addUsageAndAccessPolicyItem(new RetentionPolicyField().retentionPolicy(policy));
    return destination;
  }
}
