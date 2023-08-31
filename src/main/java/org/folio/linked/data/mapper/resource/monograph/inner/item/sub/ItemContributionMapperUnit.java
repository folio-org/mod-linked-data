package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.Bibframe2Constants.AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Collection2;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.domain.dto.ItemContributionField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = CONTRIBUTION_PRED, dtoClass = ItemContributionField2.class)
public class ItemContributionMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item2 toDto(Resource source, Item2 destination) {
    var contribution = coreMapper.readResourceDoc(source, Collection2.class);
    coreMapper.addMappedProperties(source, AGENT_PRED, contribution::addAgentItem);
    coreMapper.addMappedProperties(source, APPLICABLE_INSTITUTION_PRED, contribution::addApplicableInstitutionItem);
    destination.addContributionItem(new ItemContributionField2().contribution(contribution));
    return destination;
  }

}
