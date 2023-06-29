package org.folio.linked.data.mapper.resource.monograph.inner.item.sub.policy;

import static org.folio.linked.data.util.BibframeConstants.ITEM_USE;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.UsePolicy;
import org.folio.linked.data.domain.dto.UsePolicyField;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.item.sub.ItemSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = ITEM_USE)
public class UseMapper implements ItemSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var policy = readResourceDoc(mapper, source, UsePolicy.class);
    addMappedProperties(mapper, source, SOURCE_PRED, policy::addSourceItem);
    destination.addUsageAndAccessPolicyItem(new UsePolicyField().usePolicy(policy));
    return destination;
  }
}
