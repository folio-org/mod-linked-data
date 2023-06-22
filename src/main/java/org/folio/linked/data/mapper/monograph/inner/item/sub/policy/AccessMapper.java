package org.folio.linked.data.mapper.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_ACCESS;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessPolicy;
import org.folio.linked.data.domain.dto.AccessPolicyField;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.monograph.inner.item.sub.ItemSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(ITEM_ACCESS)
public class AccessMapper implements ItemSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var policy = readResourceDoc(mapper, source, AccessPolicy.class);
    addMappedProperties(mapper, source, SOURCE_PRED, policy::addSourceItem);
    destination.addUsageAndAccessPolicyItem(new AccessPolicyField().accessPolicy(policy));
    return destination;
  }
}
