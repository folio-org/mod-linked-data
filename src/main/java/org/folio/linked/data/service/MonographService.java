package org.folio.linked.data.service;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLIES_TO;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CHILDRENS_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.COLOR_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DDC;
import static org.folio.linked.data.util.BibframeConstants.DISSERTATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.ENUMERATION_AND_CHRONOLOGY_PRED;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.GENRE_FORM_PRED;
import static org.folio.linked.data.util.BibframeConstants.GEOGRAPHIC_COVERAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.GOVERNMENT_PUB_TYPE_PRED;
import static org.folio.linked.data.util.BibframeConstants.GRANTING_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.BibframeConstants.ILLUSTRATIVE_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.IMMEDIATE_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INTENDED_AUDIENCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ITEM_ACCESS;
import static org.folio.linked.data.util.BibframeConstants.ITEM_RETENTION;
import static org.folio.linked.data.util.BibframeConstants.ITEM_USE;
import static org.folio.linked.data.util.BibframeConstants.LANGUAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LCC;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NLM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ORIGIN_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_CLASS;
import static org.folio.linked.data.util.BibframeConstants.OTHER_EDITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.OTHER_PHYSICAL_FORMAT_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_PRED;
import static org.folio.linked.data.util.BibframeConstants.PLACE_COMPONENTS;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRIMARY_CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.RELATED_TO_PRED;
import static org.folio.linked.data.util.BibframeConstants.RELATIONSHIP_PRED;
import static org.folio.linked.data.util.BibframeConstants.RELATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.REL_WORK_LOOKUP;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBJECT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBJECT_WORK;
import static org.folio.linked.data.util.BibframeConstants.SUMMARY_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUPPLEMENTARY_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.TABLE_OF_CONTENTS_PRED;
import static org.folio.linked.data.util.BibframeConstants.TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.USAGE_AND_ACCESS_POLICY_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.WORK_TITLE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessPolicy;
import org.folio.linked.data.domain.dto.AccessPolicyField;
import org.folio.linked.data.domain.dto.ClassificationDdc;
import org.folio.linked.data.domain.dto.ClassificationDdcField;
import org.folio.linked.data.domain.dto.ClassificationLcc;
import org.folio.linked.data.domain.dto.ClassificationLccField;
import org.folio.linked.data.domain.dto.ClassificationNlm;
import org.folio.linked.data.domain.dto.ClassificationNlmField;
import org.folio.linked.data.domain.dto.ClassificationOther;
import org.folio.linked.data.domain.dto.ClassificationOtherField;
import org.folio.linked.data.domain.dto.Collection;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationField;
import org.folio.linked.data.domain.dto.DistributionField;
import org.folio.linked.data.domain.dto.Ean;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.ExtentField;
import org.folio.linked.data.domain.dto.GenreForm;
import org.folio.linked.data.domain.dto.GenreFormField;
import org.folio.linked.data.domain.dto.GeographicCoverage;
import org.folio.linked.data.domain.dto.GeographicCoverageField;
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
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemClassificationLcc;
import org.folio.linked.data.domain.dto.ItemClassificationLccField;
import org.folio.linked.data.domain.dto.ItemContributionField;
import org.folio.linked.data.domain.dto.ItemUsageAndAccessPolicyInner;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.LanguageField;
import org.folio.linked.data.domain.dto.Lccn;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LocalIdentifierField;
import org.folio.linked.data.domain.dto.ManufactureField;
import org.folio.linked.data.domain.dto.OtherIdentifierField;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.PlaceField;
import org.folio.linked.data.domain.dto.PrimaryContributionField;
import org.folio.linked.data.domain.dto.ProductionField;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.domain.dto.ProvisionActivity;
import org.folio.linked.data.domain.dto.PublicationField;
import org.folio.linked.data.domain.dto.Relationship;
import org.folio.linked.data.domain.dto.RelationshipField;
import org.folio.linked.data.domain.dto.RetentionPolicy;
import org.folio.linked.data.domain.dto.RetentionPolicyField;
import org.folio.linked.data.domain.dto.Subject;
import org.folio.linked.data.domain.dto.TopicField;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.domain.dto.UsePolicy;
import org.folio.linked.data.domain.dto.UsePolicyField;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkClassificationInner;
import org.folio.linked.data.domain.dto.WorkContributionInner;
import org.folio.linked.data.domain.dto.WorkRelationshipInner;
import org.folio.linked.data.domain.dto.WorkSubjectInner;
import org.folio.linked.data.domain.dto.WorkTitle;
import org.folio.linked.data.domain.dto.WorkTitleField;
import org.folio.linked.data.domain.dto.WorkTitleInner;
import org.folio.linked.data.exception.JsonException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonographService implements BibframeMappingService {
  
  private static final String TYPE_IS_NOT_SUPPORTED = "Provided type is not supported: ";
  private final ObjectMapper mapper = new ObjectMapper();

  private <T> T readResourceDoc(Resource resource, Class<T> dtoClass) {
    try {
      var node = resource.getDoc() != null ? resource.getDoc() : mapper.createObjectNode();
      return mapper.treeToValue(node, dtoClass);
    } catch (JsonProcessingException e) {
      throw new JsonException(e.getMessage());
    }
  }

  private Property toProperty(Resource resource) {
    return readResourceDoc(resource, Property.class);
  }

  private <T> T toDto(Resource resource, Class<T> dtoClass,
                      Map<String, BiConsumer<Resource, T>> predicate2action) {
    var dto = readResourceDoc(resource, dtoClass);
    for (var re : resource.getOutgoingEdges()) {
      var predicate = re.getPredicate().getPredicate();
      var action = predicate2action.get(predicate);
      if (action != null) {
        action.accept(re.getTarget(), dto);
      }
    }
    return dto;
  }

  @Override
  public Work toWork(Resource resource) {
    var map = new HashMap<String, BiConsumer<Resource, Work>>();
    map.put(CONTRIBUTION_PRED, (target, dto) -> dto.addContributionItem(toWorkContribution(target)));
    map.put(CLASSIFICATION_PRED, (target, dto) -> dto.addClassificationItem(toWorkClassification(target)));
    map.put(COLOR_CONTENT_PRED, (target, dto) -> dto.addColorContentItem(toProperty(target)));
    map.put(CONTENT_PRED, (target, dto) -> dto.addContentItem(toProperty(target)));
    map.put(TABLE_OF_CONTENTS_PRED, (target, dto) -> dto.addTableOfContentsItem(toProperty(target)));
    map.put(DISSERTATION_PRED, (target, dto) -> dto.addDissertationItem(toWorkDissertation(target)));
    map.put(GENRE_FORM_PRED, (target, dto) -> dto.addGenreFormItem(toGenreForm(target)));
    map.put(GEOGRAPHIC_COVERAGE_PRED, (target, dto) -> dto.addGeographicCoverageItem(toGeoCoverage(target)));
    map.put(GOVERNMENT_PUB_TYPE_PRED, (target, dto) -> dto.addGovernmentPubTypeItem(toProperty(target)));
    map.put(ILLUSTRATIVE_CONTENT_PRED, (target, dto) -> dto.addIllustrativeContentItem(toProperty(target)));
    map.put(INTENDED_AUDIENCE_PRED, (target, dto) -> dto.addIntendedAudienceItem(toProperty(target)));
    map.put(LANGUAGE_PRED, (target, dto) -> dto.addLanguageItem(toWorkLanguage(target)));
    map.put(NOTE, (target, dto) -> dto.addNoteItem(toProperty(target)));
    map.put(ORIGIN_PLACE_PRED, (target, dto) -> dto.addOriginPlaceItem(toProperty(target)));
    map.put(RELATIONSHIP_PRED, (target, dto) -> dto.addRelationshipItem(toRelationship(target)));
    map.put(SUBJECT_PRED, (target, dto) -> dto.addSubjectItem(toSubject(target)));
    map.put(SUMMARY_PRED, (target, dto) -> dto.addSummaryItem(toProperty(target)));
    map.put(SUPPLEMENTARY_CONTENT_PRED, (target, dto) -> dto.addSupplementaryContentItem(toProperty(target)));
    map.put(TITLE_PRED, (target, dto) -> dto.addTitleItem(toWorkTitle(target)));
    map.put(OTHER_EDITION_PRED, (target, dto) -> dto.addOtherEditionItem(toProperty(target)));
    map.put(OTHER_PHYSICAL_FORMAT_PRED, (target, dto) -> dto.addOtherPhysicalFormatItem(toProperty(target)));

    return toDto(resource, Work.class, map);
  }

  private WorkTitleInner toWorkTitle(Resource title) {
    switch (title.getType().getSimpleLabel()) {
      case WORK_TITLE -> {
        var titleDto = new WorkTitleField();
        titleDto.setWorkTitle(toDto(title, WorkTitle.class, Collections.emptyMap()));
        return titleDto;
      }
      case VARIANT_TITLE -> {
        var titleDto = new VariantTitleField();
        titleDto.setVariantTitle(toDto(title, VariantTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
        return titleDto;
      }
      case PARALLEL_TITLE -> {
        var titleDto = new ParallelTitleField();
        titleDto.setParallelTitle(toDto(title, ParallelTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
        return titleDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + title.getType().getSimpleLabel());
    }
  }


  private WorkSubjectInner toSubject(Resource subject) {
    var subjectInnerDto = toDto(subject, Subject.class, Map.of(
        RELATED_TO_PRED, (target, dto) -> dto.addSourceItem(toProperty(target)),
        RELATION_PRED, (target, dto) -> dto.addComponentListItem(toProperty(target))
    ));

    if (Set.of(COMPONENTS, CHILDRENS_COMPONENTS, SUBJECT_WORK).contains(subject.getType().getSimpleLabel())) {
      var subjectDto = new TopicField();
      subjectDto.setTopic(subjectInnerDto);
      return subjectDto;
    } else if (PLACE_COMPONENTS.equals(subject.getType().getSimpleLabel())) {
      var subjectDto = new PlaceField();
      subjectDto.setPlace(subjectInnerDto);
      return subjectDto;
    } else {
      throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + subject.getType().getSimpleLabel());
    }
  }

  private WorkRelationshipInner toRelationship(Resource relationship) {
    if (REL_WORK_LOOKUP.equals(relationship.getType().getSimpleLabel())) {
      var relDto = new RelationshipField();
      relDto.setRelationship(toDto(relationship, Relationship.class, Map.of(
          RELATED_TO_PRED, (target, dto) -> dto.addRelatedToItem(toProperty(target)),
          RELATION_PRED, (target, dto) -> dto.addRelationItem(toProperty(target))
      )));
      return relDto;
    }
    throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + relationship.getType().getSimpleLabel());
  }

  private LanguageField toWorkLanguage(Resource language) {
    var languageDto = new LanguageField();
    languageDto.setLanguage(toDto(language, Language.class, Map.of(
        PART_PRED, (target, dto) -> dto.addPartItem(toProperty(target)),
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
    return languageDto;
  }

  private GeographicCoverageField toGeoCoverage(Resource geoCoverage) {
    var geoCoverageDto = new GeographicCoverageField();
    geoCoverageDto.setGeographicCoverage(toDto(geoCoverage, GeographicCoverage.class, Map.of(
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
    return geoCoverageDto;
  }

  private GenreFormField toGenreForm(Resource genreForm) {
    var genreFormDto = new GenreFormField();
    genreFormDto.setGenreForm(toDto(genreForm, GenreForm.class, Map.of(
        SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target)),
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
    return genreFormDto;
  }

  private DissertationField toWorkDissertation(Resource dissertation) {
    var dissDto = new DissertationField();
    dissDto.setDissertation(toDto(dissertation, Dissertation.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)),
        DATE_PRED, (target, dto) -> dto.addDateItem(toProperty(target)),
        GRANTING_INSTITUTION_PRED, (target, dto) -> dto.addGrantingInstitutionItem(toProperty(target))
    )));
    return dissDto;
  }

  private WorkClassificationInner toWorkClassification(Resource classification) {
    switch (classification.getType().getSimpleLabel()) {
      case LCC -> {
        var classDto = new ClassificationLccField();
        classDto.setClassificationLcc(toDto(classification, ClassificationLcc.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target)),
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
        return classDto;
      }
      case DDC -> {
        var classDto = new ClassificationDdcField();
        classDto.setClassificationDdc(toDto(classification, ClassificationDdc.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target)),
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
        return classDto;
      }
      case NLM -> {
        var classDto = new ClassificationNlmField();
        classDto.setClassificationNlm(toDto(classification, ClassificationNlm.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
        return classDto;
      }
      case OTHER_CLASS -> {
        var classDto = new ClassificationOtherField();
        classDto.setOtherClassification(toDto(classification, ClassificationOther.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
        return classDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + classification.getType().getSimpleLabel());
    }
  }

  private WorkContributionInner toWorkContribution(Resource contrib) {
    Map<String, BiConsumer<Resource, Contribution>> map = Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        ROLE_PRED, (target, dto) -> dto.addRoleItem(toProperty(target)));
    if (PRIMARY_CONTRIBUTION.equals(contrib.getType().getSimpleLabel())) {
      var contribDto = new PrimaryContributionField();
      contribDto.setPrimaryContribution(toDto(contrib, Contribution.class, map));
      return contribDto;
    } else if (CONTRIBUTION.equals(contrib.getType().getSimpleLabel())) {
      var contribDto = new ContributionField();
      contribDto.setContribution(toDto(contrib, Contribution.class, map));
      return contribDto;
    } else {
      throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + contrib.getType().getSimpleLabel());
    }
  }

  @Override
  public Instance toInstance(Resource instance) {
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

    return toDto(instance, Instance.class, map);
  }

  private UrlField toElectronicLocator(Resource electronicLocator) {
    var electronicLocDto = new UrlField();
    electronicLocDto.setUrl(toDto(electronicLocator, Url.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)))));
    return electronicLocDto;
  }

  private ExtentField toExtent(Resource extent) {
    var extentDto = new ExtentField();
    extentDto.setExtent(toDto(extent, Extent.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)),
        APPLIES_TO, (target, dto) -> dto.addAppliesToItem(toProperty(target))
    )));
    return extentDto;
  }

  private ImmediateAcquisitionField toImmediateAcquisition(Resource immediateAcquisition) {
    var immediateAcquisitionDto = new ImmediateAcquisitionField();
    immediateAcquisitionDto.setImmediateAcquisition(toDto(immediateAcquisition, ImmediateAcquisition.class, Map.of(
        APPLICABLE_INSTITUTION_PRED, (target, dto) -> dto.addApplicableInstitutionItem(toProperty(target)))));
    return immediateAcquisitionDto;
  }

  private InstanceIdentifiedByInner toInstanceIdentifiedBy(Resource identifiedBy) {
    switch (identifiedBy.getType().getSimpleLabel()) {
      case IDENTIFIERS_LCCN -> {
        var identifierDto = new LccnField();
        identifierDto.setLccn(toDto(identifiedBy, Lccn.class, Map.of(
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
        return identifierDto;
      }
      case IDENTIFIERS_ISBN -> {
        var identifierDto = new IsbnField();
        identifierDto.setIsbn(toDto(identifiedBy, Isbn.class, Map.of(
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
        return identifierDto;
      }
      case IDENTIFIERS_EAN -> {
        var classDto = new EanField();
        classDto.setEan(toDto(identifiedBy, Ean.class, Collections.emptyMap()));
        return classDto;
      }
      case IDENTIFIERS_LOCAL -> {
        var identifierDto = new LocalIdentifierField();
        identifierDto.setLocal(toDto(identifiedBy, IdentifierLocal.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
        return identifierDto;
      }
      case IDENTIFIERS_OTHER -> {
        var identifierDto = new OtherIdentifierField();
        identifierDto.setIdentifier(toDto(identifiedBy, IdentifierOther.class, Collections.emptyMap()));
        return identifierDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + identifiedBy.getType().getSimpleLabel());
    }
  }

  private InstanceProvisionActivityInner toInstanceProvisionActivity(Resource provActivity) {
    switch (provActivity.getType().getSimpleLabel()) {
      case PUBLICATION -> {
        var provActivityDto = new PublicationField();
        provActivityDto.setPublication(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
        return provActivityDto;
      }
      case DISTRIBUTION -> {
        var provActivityDto = new DistributionField();
        provActivityDto.setDistribution(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
        return provActivityDto;
      }
      case MANUFACTURE -> {
        var provActivityDto = new ManufactureField();
        provActivityDto.setManufacture(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
        return provActivityDto;
      }
      case PRODUCTION -> {
        var provActivityDto = new ProductionField();
        provActivityDto.setProduction(toDto(provActivity, ProvisionActivity.class, Map.of(
            PLACE_PRED, (target, dto) -> dto.addPlaceItem(toProperty(target))
        )));
        return provActivityDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + provActivity.getType().getSimpleLabel());
    }
  }

  private ContributionField toInstanceContribution(Resource contrib) {
    var contribDto = new ContributionField();
    contribDto.setContribution(toDto(contrib, Contribution.class, Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        ROLE_PRED, (target, dto) -> dto.addRoleItem(toProperty(target)))));
    return contribDto;
  }

  private InstanceTitleInner toInstanceTitle(Resource title) {
    switch (title.getType().getSimpleLabel()) {
      case INSTANCE_TITLE -> {
        var titleDto = new InstanceTitleField();
        titleDto.setInstanceTitle(toDto(title, InstanceTitle.class, Collections.emptyMap()));
        return titleDto;
      }
      case VARIANT_TITLE -> {
        var titleDto = new VariantTitleField();
        titleDto.setVariantTitle(toDto(title, VariantTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
        return titleDto;
      }
      case PARALLEL_TITLE -> {
        var titleDto = new ParallelTitleField();
        titleDto.setParallelTitle(toDto(title, ParallelTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
        return titleDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + title.getType().getSimpleLabel());
    }
  }

  @Override
  public Item toItem(Resource item) {
    var map = new HashMap<String, BiConsumer<Resource, Item>>();
    map.put(CONTRIBUTION_PRED, (target, dto) -> dto.addContributionItem(toItemContribution(target)));
    map.put(ENUMERATION_AND_CHRONOLOGY_PRED, (target, dto) -> dto.addEnumerationAndChronologyItem(toProperty(target)));
    map.put(USAGE_AND_ACCESS_POLICY_PRED, (target, dto) ->
        dto.addUsageAndAccessPolicyItem(toUsageAndAccessPolicy(target)));
    map.put(NOTE, (target, dto) -> dto.addNoteItem(toProperty(target)));
    map.put(CLASSIFICATION_PRED, (target, dto) -> dto.addClassificationItem(toItemClassification(target)));
    map.put(ELECTRONIC_LOCATOR_PRED, (target, dto) -> dto.addElectronicLocatorItem(toElectronicLocator(target)));

    return toDto(item, Item.class, map);
  }

  private ItemUsageAndAccessPolicyInner toUsageAndAccessPolicy(Resource policy) {
    switch (policy.getType().getSimpleLabel()) {
      case ITEM_ACCESS -> {
        var policyDto = new AccessPolicyField();
        policyDto.setAccessPolicy(toDto(policy, AccessPolicy.class, Map.of(
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
        return policyDto;
      }
      case ITEM_USE -> {
        var policyDto = new UsePolicyField();
        policyDto.setUsePolicy(toDto(policy, UsePolicy.class, Map.of(
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
        return policyDto;
      }
      case ITEM_RETENTION -> {
        var policyDto = new RetentionPolicyField();
        policyDto.setRetentionPolicy(toDto(policy, RetentionPolicy.class, Collections.emptyMap()));
        return policyDto;
      }
      default -> throw new NotSupportedException(TYPE_IS_NOT_SUPPORTED + policy.getType().getSimpleLabel());
    }
  }

  private ItemClassificationLccField toItemClassification(Resource classification) {
    var classDto = new ItemClassificationLccField();
    classDto.setClassificationLcc(toDto(classification, ItemClassificationLcc.class, Map.of(
        NOTE, (target, dto) -> dto.addNoteItem(toProperty(target))
    )));
    return classDto;
  }

  private ItemContributionField toItemContribution(Resource contrib) {
    var contribDto = new ItemContributionField();
    contribDto.setContribution(toDto(contrib, Collection.class, Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        APPLICABLE_INSTITUTION_PRED, (target, dto) -> dto.addApplicableInstitutionItem(toProperty(target)))));
    return contribDto;
  }

}
