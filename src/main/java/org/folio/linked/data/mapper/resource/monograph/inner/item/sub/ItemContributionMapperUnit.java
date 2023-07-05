package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Collection;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemContributionField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = CONTRIBUTION_PRED, dtoClass = ItemContributionField.class)
public class ItemContributionMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var contribution = coreMapper.readResourceDoc(source, Collection.class);
    coreMapper.addMappedProperties(source, AGENT_PRED, contribution::addAgentItem);
    coreMapper.addMappedProperties(source, APPLICABLE_INSTITUTION_PRED, contribution::addApplicableInstitutionItem);
    destination.addContributionItem(new ItemContributionField().contribution(contribution));
    return destination;
  }

}
