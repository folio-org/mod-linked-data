package org.folio.linked.data.mapper.monograph;

import static org.folio.linked.data.mapper.ResourceMapper.IS_NOT_SUPPORTED_HERE;
import static org.folio.linked.data.mapper.ResourceMapper.RESOURCE_TYPE;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
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
import static org.folio.linked.data.util.BibframeConstants.GENRE_FORM_PRED;
import static org.folio.linked.data.util.BibframeConstants.GEOGRAPHIC_COVERAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.GOVERNMENT_PUB_TYPE_PRED;
import static org.folio.linked.data.util.BibframeConstants.GRANTING_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ILLUSTRATIVE_CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.INTENDED_AUDIENCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LANGUAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LCC;
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
import static org.folio.linked.data.util.BibframeConstants.PRIMARY_CONTRIBUTION;
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
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.WORK_TITLE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.folio.linked.data.domain.dto.ClassificationDdc;
import org.folio.linked.data.domain.dto.ClassificationDdcField;
import org.folio.linked.data.domain.dto.ClassificationLcc;
import org.folio.linked.data.domain.dto.ClassificationLccField;
import org.folio.linked.data.domain.dto.ClassificationNlm;
import org.folio.linked.data.domain.dto.ClassificationNlmField;
import org.folio.linked.data.domain.dto.ClassificationOther;
import org.folio.linked.data.domain.dto.ClassificationOtherField;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.ContributionField;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationField;
import org.folio.linked.data.domain.dto.GenreForm;
import org.folio.linked.data.domain.dto.GenreFormField;
import org.folio.linked.data.domain.dto.GeographicCoverage;
import org.folio.linked.data.domain.dto.GeographicCoverageField;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.LanguageField;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.PlaceField;
import org.folio.linked.data.domain.dto.PrimaryContributionField;
import org.folio.linked.data.domain.dto.Relationship;
import org.folio.linked.data.domain.dto.RelationshipField;
import org.folio.linked.data.domain.dto.Subject;
import org.folio.linked.data.domain.dto.TopicField;
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
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.WorkMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class MonographWorkMapper extends BaseBibframeMapper implements WorkMapper {

  public MonographWorkMapper(ObjectMapper mapper) {
    super(mapper);
  }

  @Override
  public Work toWork(Resource resource) {
    return toDto(resource, Work.class, getPredicate2ActionMap());
  }

  private Map<String, BiConsumer<Resource, Work>> getPredicate2ActionMap() {
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
    return map;
  }

  private WorkTitleInner toWorkTitle(Resource title) {
    switch (title.getType().getSimpleLabel()) {
      case WORK_TITLE -> {
        return new WorkTitleField().workTitle(toDto(title, WorkTitle.class, Collections.emptyMap()));
      }
      case VARIANT_TITLE -> {
        return new VariantTitleField().variantTitle(toDto(title, VariantTitle.class, Map.of(
            NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target))
        )));
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


  private WorkSubjectInner toSubject(Resource subject) {
    var subjectInnerDto = toDto(subject, Subject.class, Map.of(
        RELATED_TO_PRED, (target, dto) -> dto.addSourceItem(toProperty(target)),
        RELATION_PRED, (target, dto) -> dto.addComponentListItem(toProperty(target))
    ));

    if (Set.of(COMPONENTS, CHILDRENS_COMPONENTS, SUBJECT_WORK).contains(subject.getType().getSimpleLabel())) {
      return new TopicField().topic(subjectInnerDto);
    } else if (PLACE_COMPONENTS.equals(subject.getType().getSimpleLabel())) {
      return new PlaceField().place(subjectInnerDto);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + subject.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

  private WorkRelationshipInner toRelationship(Resource relationship) {
    if (REL_WORK_LOOKUP.equals(relationship.getType().getSimpleLabel())) {
      return new RelationshipField().relationship(toDto(relationship, Relationship.class, Map.of(
          RELATED_TO_PRED, (target, dto) -> dto.addRelatedToItem(toProperty(target)),
          RELATION_PRED, (target, dto) -> dto.addRelationItem(toProperty(target))
      )));
    }
    throw new NotSupportedException(RESOURCE_TYPE + relationship.getType().getSimpleLabel()
        + IS_NOT_SUPPORTED_HERE);
  }

  private LanguageField toWorkLanguage(Resource language) {
    return new LanguageField().language(toDto(language, Language.class, Map.of(
        PART_PRED, (target, dto) -> dto.addPartItem(toProperty(target)),
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
  }

  private GeographicCoverageField toGeoCoverage(Resource geoCoverage) {
    return new GeographicCoverageField().geographicCoverage(toDto(geoCoverage, GeographicCoverage.class, Map.of(
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
  }

  private GenreFormField toGenreForm(Resource genreForm) {
    return new GenreFormField().genreForm(toDto(genreForm, GenreForm.class, Map.of(
        SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target)),
        SAME_AS_PRED, (target, dto) -> dto.addSameAsItem(toProperty(target))
    )));
  }

  private DissertationField toWorkDissertation(Resource dissertation) {
    return new DissertationField().dissertation(toDto(dissertation, Dissertation.class, Map.of(
        NOTE_PRED, (target, dto) -> dto.addNoteItem(toProperty(target)),
        DATE_PRED, (target, dto) -> dto.addDateItem(toProperty(target)),
        GRANTING_INSTITUTION_PRED, (target, dto) -> dto.addGrantingInstitutionItem(toProperty(target))
    )));
  }

  private WorkClassificationInner toWorkClassification(Resource classification) {
    switch (classification.getType().getSimpleLabel()) {
      case LCC -> {
        return new ClassificationLccField().classificationLcc(toDto(classification, ClassificationLcc.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target)),
            STATUS_PRED, (target, dto) -> dto.addStatusItem(toProperty(target))
        )));
      }
      case DDC -> {
        return new ClassificationDdcField().classificationDdc(toDto(classification, ClassificationDdc.class, Map.of(
            ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target)),
            SOURCE_PRED, (target, dto) -> dto.addSourceItem(toProperty(target))
        )));
      }
      case NLM -> {
        return new ClassificationNlmField().classificationNlm(toDto(classification, ClassificationNlm.class,
            Map.of(ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
      }
      case OTHER_CLASS -> {
        return new ClassificationOtherField().otherClassification(toDto(classification, ClassificationOther.class,
            Map.of(ASSIGNER_PRED, (target, dto) -> dto.addAssignerItem(toProperty(target))
        )));
      }
      default -> throw new NotSupportedException(RESOURCE_TYPE + classification.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

  private WorkContributionInner toWorkContribution(Resource contrib) {
    Map<String, BiConsumer<Resource, Contribution>> map = Map.of(
        AGENT_PRED, (target, dto) -> dto.addAgentItem(toProperty(target)),
        ROLE_PRED, (target, dto) -> dto.addRoleItem(toProperty(target)));
    if (PRIMARY_CONTRIBUTION.equals(contrib.getType().getSimpleLabel())) {
      return new PrimaryContributionField().primaryContribution(toDto(contrib, Contribution.class, map));
    } else if (CONTRIBUTION.equals(contrib.getType().getSimpleLabel())) {
      return new ContributionField().contribution(toDto(contrib, Contribution.class, map));
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + contrib.getType().getSimpleLabel()
          + IS_NOT_SUPPORTED_HERE);
    }
  }

}
