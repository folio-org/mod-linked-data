package org.folio.linked.data.mapper.monograph.inner.work.sub.contribution;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRIMARY_CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.PrimaryContributionField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(PRIMARY_CONTRIBUTION)
public class PrimaryMapper implements WorkSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var contribution = readResourceDoc(mapper, source, Contribution.class);
    addMappedProperties(mapper, source, AGENT_PRED, contribution::addAgentItem);
    addMappedProperties(mapper, source, ROLE_PRED, contribution::addRoleItem);
    destination.addContributionItem(new PrimaryContributionField().primaryContribution(contribution));
    return destination;
  }
}
