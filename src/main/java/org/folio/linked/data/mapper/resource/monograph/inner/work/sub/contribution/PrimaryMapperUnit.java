package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contribution;

import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRIMARY_CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution2;
import org.folio.linked.data.domain.dto.PrimaryContributionField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PRIMARY_CONTRIBUTION, predicate = CONTRIBUTION_PRED, dtoClass = PrimaryContributionField2.class)
public class PrimaryMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var contribution = coreMapper.readResourceDoc(source, Contribution2.class);
    //TODO map agent
    coreMapper.addMappedProperties(source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new PrimaryContributionField2().primaryContribution(contribution));
    return destination;
  }
}
