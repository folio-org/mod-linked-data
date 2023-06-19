package org.folio.linked.data.mapper.monograph;

import static org.folio.linked.data.mapper.ResourceMapper.IS_NOT_SUPPORTED_HERE;
import static org.folio.linked.data.mapper.ResourceMapper.RESOURCE_TYPE;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.ENUMERATION_AND_CHRONOLOGY_PRED;
import static org.folio.linked.data.util.BibframeConstants.ITEM_ACCESS;
import static org.folio.linked.data.util.BibframeConstants.ITEM_RETENTION;
import static org.folio.linked.data.util.BibframeConstants.ITEM_USE;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.USAGE_AND_ACCESS_POLICY_PRED;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.folio.linked.data.domain.dto.AccessPolicy;
import org.folio.linked.data.domain.dto.AccessPolicyField;
import org.folio.linked.data.domain.dto.Collection;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemClassificationLcc;
import org.folio.linked.data.domain.dto.ItemClassificationLccField;
import org.folio.linked.data.domain.dto.ItemContributionField;
import org.folio.linked.data.domain.dto.ItemUsageAndAccessPolicyInner;
import org.folio.linked.data.domain.dto.RetentionPolicy;
import org.folio.linked.data.domain.dto.RetentionPolicyField;
import org.folio.linked.data.domain.dto.UsePolicy;
import org.folio.linked.data.domain.dto.UsePolicyField;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.ItemMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class MonographItemMapper extends BaseBibframeMapper implements ItemMapper {

  public MonographItemMapper(ObjectMapper mapper) {
    super(mapper);
  }

  @Override
  public Item toItem(Resource item) {
    return toDto(item, Item.class, getPredicate2ActionMap());
  }

  private Map<String, BiConsumer<Resource, Item>> getPredicate2ActionMap() {
    var map = new HashMap<String, BiConsumer<Resource, Item>>();
    map.put(CONTRIBUTION_PRED, (target, dto) -> dto.addContributionItem(toItemContribution(target)));
    map.put(ENUMERATION_AND_CHRONOLOGY_PRED, (target, dto) -> dto.addEnumerationAndChronologyItem(toProperty(target)));
    map.put(USAGE_AND_ACCESS_POLICY_PRED, (target, dto) ->
        dto.addUsageAndAccessPolicyItem(toUsageAndAccessPolicy(target)));
    map.put(NOTE, (target, dto) -> dto.addNoteItem(toProperty(target)));
    map.put(CLASSIFICATION_PRED, (target, dto) -> dto.addClassificationItem(toItemClassification(target)));
    map.put(ELECTRONIC_LOCATOR_PRED, (target, dto) -> dto.addElectronicLocatorItem(toElectronicLocator(target)));
    return map;
  }

  private ItemUsageAndAccessPolicyInner toUsageAndAccessPolicy(Resource policy) {
    switch (policy.getType().getSimpleLabel()) {
      case ITEM_ACCESS -> {
        return new AccessPolicyField().accessPolicy(toDto(policy, AccessPolicy.class, Map.of(
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
      }
      case ITEM_USE -> {
        return new UsePolicyField().usePolicy(toDto(policy, UsePolicy.class, Map.of(
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
      }
      case ITEM_RETENTION -> {
        return new RetentionPolicyField().retentionPolicy(toDto(policy, RetentionPolicy.class, Collections.emptyMap()));
      }
      default -> throw new NotSupportedException(RESOURCE_TYPE + policy.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

  private ItemClassificationLccField toItemClassification(Resource classification) {
    return new ItemClassificationLccField().classificationLcc(toDto(classification, ItemClassificationLcc.class,
        Map.of(NOTE, (target, dto) -> dto.addNoteItem(toProperty(target)))));
  }

  private ItemContributionField toItemContribution(Resource contrib) {
    return new ItemContributionField().contribution(toDto(contrib, Collection.class, Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        APPLICABLE_INSTITUTION_PRED, (target, dto) -> dto.addApplicableInstitutionItem(toProperty(target)))));
  }

}
