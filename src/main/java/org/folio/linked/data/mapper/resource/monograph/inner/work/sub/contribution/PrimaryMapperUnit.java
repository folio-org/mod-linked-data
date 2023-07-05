package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contribution;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRIMARY_CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.PrimaryContributionField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PRIMARY_CONTRIBUTION, predicate = CONTRIBUTION_PRED, dtoClass = PrimaryContributionField.class)
public class PrimaryMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var contribution = coreMapper.readResourceDoc(source, Contribution.class);
    coreMapper.addMappedPersonLookups(source, AGENT_PRED, contribution::addAgentItem);
    coreMapper.addMappedProperties(source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new PrimaryContributionField().primaryContribution(contribution));
    return destination;
  }
}
