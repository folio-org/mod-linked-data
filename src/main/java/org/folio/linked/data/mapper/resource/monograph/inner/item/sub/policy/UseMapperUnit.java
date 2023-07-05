package org.folio.linked.data.mapper.resource.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_USE;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.UsePolicy;
import org.folio.linked.data.domain.dto.UsePolicyField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.item.sub.ItemSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ITEM_USE)
public class UseMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var policy = coreMapper.readResourceDoc(source, UsePolicy.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, policy::addSourceItem);
    destination.addUsageAndAccessPolicyItem(new UsePolicyField().usePolicy(policy));
    return destination;
  }
}
