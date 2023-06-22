package org.folio.linked.data.mapper.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Collection;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemContributionField;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(CONTRIBUTION_PRED)
public class ItemContributionMapper implements ItemSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var contribution = readResourceDoc(objectMapper, source, Collection.class);
    addMappedProperties(objectMapper, source, AGENT_PRED, contribution::addAgentItem);
    addMappedProperties(objectMapper, source, APPLICABLE_INSTITUTION_PRED, contribution::addApplicableInstitutionItem);
    destination.addContributionItem(new ItemContributionField().contribution(contribution));
    return destination;
  }

}
