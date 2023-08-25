package org.folio.linked.data.mapper.resource.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_ACCESS;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessPolicy2;
import org.folio.linked.data.domain.dto.AccessPolicyField2;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.item.sub.ItemSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ITEM_ACCESS)
public class AccessMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item2 toDto(Resource source, Item2 destination) {
    var policy = coreMapper.readResourceDoc(source, AccessPolicy2.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, policy::addSourceItem);
    destination.addUsageAndAccessPolicyItem(new AccessPolicyField2().accessPolicy(policy));
    return destination;
  }
}
