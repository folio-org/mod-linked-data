package org.folio.linked.data.mapper.monograph;

import static org.folio.linked.data.mapper.BibframeMapper.IS_NOT_SUPPORTED_HERE;
import static org.folio.linked.data.mapper.BibframeMapper.RESOURCE_TYPE;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.BibframeConstants.IMMEDIATE_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPPLEMENTARY_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.Ean;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.ExtentField;
import org.folio.linked.data.domain.dto.IdentifierLocal;
import org.folio.linked.data.domain.dto.IdentifierOther;
import org.folio.linked.data.domain.dto.ImmediateAcquisition;
import org.folio.linked.data.domain.dto.ImmediateAcquisitionField;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceIdentifiedByInner;
import org.folio.linked.data.domain.dto.InstanceProvisionActivityInner;
import org.folio.linked.data.domain.dto.InstanceTitle;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.domain.dto.InstanceTitleInner;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
import org.folio.linked.data.domain.dto.ManufactureField;
import org.folio.linked.data.domain.dto.OtherIdentifierField;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.ProductionField;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.PublicationField;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.BaseResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class MonographInstanceMapperBase extends BaseResourceMapper<Instance> {

  public MonographInstanceMapperBase(ObjectMapper mapper) {
    super(mapper);
  }

  @Override
  public Instance map(Resource resource) {
    return map(resource, Instance.class);
  }

  @Override
  protected Map<String, BiConsumer<Resource, Instance>> initPred2Action() {
    var map = new HashMap<String, BiConsumer<Resource, Instance>>();
    map.put(TITLE_PRED, (target, dto) -> dto.addTitleItem(toInstanceTitle(target)));
    map.put(PROVISION_ACTIVITY_PRED, (target, dto) ->
        dto.addProvisionActivityItem(toInstanceProvisionActivity(target)));
    map.put(CONTRIBUTION_PRED, (target, dto) -> dto.addContributionItem(toInstanceContribution(target)));
    map.put(IDENTIFIED_BY_PRED, (target, dto) -> dto.addIdentifiedByItem(toInstanceIdentifiedBy(target)));
    map.put(NOTE, (target, dto) -> dto.addNoteItem(toProperty(target)));
    map.put(SUPPLEMENTARY_CONTENT_PRED, (target, dto) -> dto.addSupplementaryContentItem(toProperty(target)));
    map.put(IMMEDIATE_ACQUISITION_PRED, (target, dto) ->
        dto.addImmediateAcquisitionItem(toImmediateAcquisition(target)));
    map.put(EXTENT_PRED, (target, dto) -> dto.addExtentItem(toExtent(target)));
    map.put(ELECTRONIC_LOCATOR_PRED, (target, dto) -> dto.addElectronicLocatorItem(toElectronicLocator(target)));
    map.put(ISSUANCE_PRED, (target, dto) -> dto.addIssuanceItem(toProperty(target)));
    map.put(MEDIA_PRED, (target, dto) -> dto.addMediaItem(toProperty(target)));
    map.put(CARRIER_PRED, (target, dto) -> dto.addCarrierItem(toProperty(target)));
    return map;
  }

  private ExtentField toExtent(Resource extent) {
    return new ExtentField().extent(toDto(extent, Extent.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)),
        APPLIES_TO, (target, dto) -> dto.addAppliesToItem(toProperty(target))
    )));
  }

  private ImmediateAcquisitionField toImmediateAcquisition(Resource immediateAcquisition) {
    return new ImmediateAcquisitionField().immediateAcquisition(toDto(
        immediateAcquisition, ImmediateAcquisition.class, Map.of(
            APPLICABLE_INSTITUTION_PRED, (target, dto) -> dto.addApplicableInstitutionItem(toProperty(target)))));
  }

  private InstanceIdentifiedByInner toInstanceIdentifiedBy(Resource identifiedBy) {
    switch (identifiedBy.getType().getSimpleLabel()) {
      case IDENTIFIERS_LCCN -> {
        return new LccnField().lccn(toDto(identifiedBy, Lccn.class, Map.of(
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
      }
      case IDENTIFIERS_ISBN -> {
        return new IsbnField().isbn(toDto(identifiedBy, Isbn.class, Map.of(
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
      }
      case IDENTIFIERS_EAN -> {
        return new EanField().ean(toDto(identifiedBy, Ean.class, Collections.emptyMap()));
      }
      case IDENTIFIERS_LOCAL -> {
        return new LocalIdentifierField().local(toDto(identifiedBy, IdentifierLocal.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
      }
      case IDENTIFIERS_OTHER -> {
        return new OtherIdentifierField().identifier(toDto(identifiedBy, IdentifierOther.class,
            Collections.emptyMap()));
      }
      default -> throw new NotSupportedException(RESOURCE_TYPE + identifiedBy.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

  private InstanceProvisionActivityInner toInstanceProvisionActivity(Resource provActivity) {
    switch (provActivity.getType().getSimpleLabel()) {
      case PUBLICATION -> {
        return new PublicationField().publication(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
      }
      case DISTRIBUTION -> {
        return new DistributionField().distribution(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
      }
      case MANUFACTURE -> {
        return new ManufactureField().manufacture(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
      }
      case PRODUCTION -> {
        return new ProductionField().production(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
      }
      default -> throw new NotSupportedException(RESOURCE_TYPE + provActivity.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

  private ContributionField toInstanceContribution(Resource contrib) {
    return new ContributionField().contribution(toDto(contrib, Contribution.class, Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        ROLE_PRED, (target, dto) -> dto.addRoleItem(toProperty(target)))));
  }

  private InstanceTitleInner toInstanceTitle(Resource title) {
    switch (title.getType().getSimpleLabel()) {
      case INSTANCE_TITLE -> {
        return new InstanceTitleField().instanceTitle(toDto(title, InstanceTitle.class, Collections.emptyMap()));
      }
      case VARIANT_TITLE -> {
        return new VariantTitleField().variantTitle(toDto(title, VariantTitle.class, Map.of()));
      }
      case PARALLEL_TITLE -> {
        return new ParallelTitleField().parallelTitle(toDto(title, ParallelTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
      }
      default -> throw new NotSupportedException(RESOURCE_TYPE + title.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

}
