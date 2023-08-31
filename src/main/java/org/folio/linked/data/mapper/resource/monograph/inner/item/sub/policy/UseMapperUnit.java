package org.folio.linked.data.mapper.resource.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.Bibframe2Constants.ITEM_USE;
import static org.folio.linked.data.util.Bibframe2Constants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.domain.dto.UsePolicy2;
import org.folio.linked.data.domain.dto.UsePolicyField2;
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
  public Item2 toDto(Resource source, Item2 destination) {
    var policy = coreMapper.readResourceDoc(source, UsePolicy2.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, policy::addSourceItem);
    destination.addUsageAndAccessPolicyItem(new UsePolicyField2().usePolicy(policy));
    return destination;
  }
}
