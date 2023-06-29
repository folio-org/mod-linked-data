package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Collection;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemContributionField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = CONTRIBUTION_PRED, dtoClass = ItemContributionField.class)
public class ItemContributionMapper implements ItemSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var contribution = commonMapper.readResourceDoc(source, Collection.class);
    commonMapper.addMappedProperties(source, AGENT_PRED, contribution::addAgentItem);
    commonMapper.addMappedProperties(source, APPLICABLE_INSTITUTION_PRED, contribution::addApplicableInstitutionItem);
    destination.addContributionItem(new ItemContributionField().contribution(contribution));
    return destination;
  }

}
