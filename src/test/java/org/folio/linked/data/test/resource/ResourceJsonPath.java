package org.folio.linked.data.test.resource;

import static java.lang.String.join;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_END;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_START;
import static org.folio.ld.dictionary.PropertyDictionary.DEGREE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_ID;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_YEAR;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ITEM_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.MATERIALS_SPECIFIED;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.STATEMENT_OF_RESPONSIBILITY;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;

import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;

@UtilityClass
public class ResourceJsonPath {

  private static final String ID_PROPERTY = "id";
  private static final String LABEL_PROPERTY = "label";
  private static final String IS_PREFERRED_PROPERTY = "isPreferred";
  private static final String INSTANCE_REF = "_instanceReference";
  private static final String WORK_REF = "_workReference";
  private static final String GEOGRAPHIC_COVERAGE_REF = "_geographicCoverageReference";
  private static final String GENRE_REF = "_genreReference";
  private static final String ASSIGNING_SOURCE_REF = "_assigningSourceReference";
  private static final String GRANTING_INSTITUTION_REF = "_grantingInstitutionReference";
  private static final String EXTENT_V2_URI = "http://bibfra.me/vocab/lite/extentV2";

  public static String toInstance() {
    return join(".", "$", path("resource"), path(INSTANCE.getUri()));
  }

  public static String toInstanceReference(String workBase) {
    return join(".", workBase, arrayPath(INSTANCE_REF));
  }

  public static String toWork() {
    return join(".", "$", path("resource"), path(WORK.getUri()));
  }

  public static String toCreatorReferenceId() {
    return join(".", toWork(), "_creatorReference[0]", "id");
  }

  public static String toWorkReference() {
    return join(".", toInstance(), arrayPath(WORK_REF));
  }

  public static String toExtent() {
    return String.join(".", toInstance(), arrayPath(EXTENT.getValue()));
  }

  public static String toDimensions() {
    return join(".", toInstance(), arrayPath(DIMENSIONS.getValue()));
  }

  public static String toEditionStatement() {
    return join(".", toInstance(), arrayPath(EDITION.getValue()));
  }

  public static String toSupplementaryContentLink() {
    return join(".", toInstance(), arrayPath(SUPPLEMENTARY_CONTENT.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toSupplementaryContentName() {
    return join(".", toInstance(), arrayPath(SUPPLEMENTARY_CONTENT.getUri()), arrayPath(NAME.getValue()));
  }

  public static String toExtentLabel() {
    return join(".", toInstance(), arrayPath(EXTENT_V2_URI), arrayPath(LABEL.getValue()));
  }

  public static String toExtentMaterialsSpec() {
    return join(".", toInstance(), arrayPath(EXTENT_V2_URI),
      arrayPath(MATERIALS_SPECIFIED.getValue()));
  }

  public static String toExtentNote() {
    return join(".", toInstance(), arrayPath(EXTENT_V2_URI), arrayPath(NOTE.getValue()));
  }

  public static String toAccessLocationLink() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toAccessLocationNote() {
    return join(".", toInstance(), arrayPath(ACCESS_LOCATION.getUri()), arrayPath(NOTE.getValue()));
  }

  public static String toProjectedProvisionDate() {
    return join(".", toInstance(), arrayPath(PROJECTED_PROVISION_DATE.getValue()));
  }

  public static String toPrimaryTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NAME.getValue()));
  }

  public static String toPrimaryTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(PART_NUMBER.getValue()));
  }

  public static String toPrimaryTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(MAIN_TITLE.getValue()));
  }

  public static String toPrimaryTitleNonSortNum(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(NON_SORT_NUM.getValue()));
  }

  public static String toPrimaryTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()),
      path(ResourceTypeDictionary.TITLE.getUri()), arrayPath(SUBTITLE.getValue()));
  }

  public static String toIssuance() {
    return join(".", toInstance(), arrayPath(ISSUANCE.getValue()));
  }

  public static String toStatementOfResponsibility() {
    return join(".", toInstance(), arrayPath(STATEMENT_OF_RESPONSIBILITY.getValue()));
  }

  public static String toParallelTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  public static String toParallelTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  public static String toParallelTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  public static String toParallelTitleDate(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  public static String toParallelTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  public static String toParallelTitleNote(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(PARALLEL_TITLE.getUri()),
      arrayPath(NOTE.getValue()));
  }

  public static String toVariantTitlePartName(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NAME.getValue()));
  }

  public static String toVariantTitlePartNumber(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(PART_NUMBER.getValue()));
  }

  public static String toVariantTitleMain(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(MAIN_TITLE.getValue()));
  }

  public static String toVariantTitleDate(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(DATE.getValue()));
  }

  public static String toVariantTitleSubtitle(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(SUBTITLE.getValue()));
  }

  public static String toVariantTitleType(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(VARIANT_TYPE.getValue()));
  }

  public static String toVariantTitleNote(String instanceBase) {
    return join(".", instanceBase, dynamicArrayPath(TITLE.getUri()), path(VARIANT_TITLE.getUri()),
      arrayPath(NOTE.getValue()));
  }

  public static String toProviderEventDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(DATE.getValue()));
  }

  public static String toProviderEventName(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(NAME.getValue()));
  }

  public static String toProviderEventPlaceCode(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(CODE.getValue()));
  }

  public static String toProviderEventPlaceLabel(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(LABEL.getValue()));
  }

  public static String toProviderEventPlaceLink(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_PLACE.getUri()),
      arrayPath(LINK.getValue()));
  }

  public static String toProviderEventProviderDate(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(PROVIDER_DATE.getValue()));
  }

  public static String toProviderEventSimplePlace(PredicateDictionary predicate) {
    return join(".", toInstance(), arrayPath(predicate.getUri()), arrayPath(SIMPLE_PLACE.getValue()));
  }

  public static String toLccnValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(NAME.getValue()));
  }

  public static String toLccnStatusValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL.getValue()));
  }

  public static String toLccnStatusLink() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LCCN.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LINK.getValue()));
  }

  public static String toIsbnValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()), arrayPath(NAME.getValue()));
  }

  public static String toIsbnQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  public static String toIsbnStatusValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LABEL.getValue()));
  }

  public static String toIsbnStatusLink() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_ISBN.getUri()),
      arrayPath(STATUS.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toEanValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_EAN.getUri()),
      arrayPath(EAN_VALUE.getValue()));
  }

  public static String toEanQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_EAN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  public static String toLocalIdValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LOCAL.getUri()),
      arrayPath(LOCAL_ID_VALUE.getValue()));
  }

  public static String toLocalIdAssigner() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_LOCAL.getUri()),
      arrayPath(ASSIGNING_SOURCE.getValue()));
  }

  public static String toOtherIdValue() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_UNKNOWN.getUri()),
      arrayPath(NAME.getValue()));
  }

  public static String toOtherIdQualifier() {
    return join(".", toInstance(), dynamicArrayPath(MAP.getUri()), path(ID_UNKNOWN.getUri()),
      arrayPath(QUALIFIER.getValue()));
  }

  public static String toCarrierCode(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toCarrierLink(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toCarrierTerm(String instanceBase) {
    return join(".", instanceBase, arrayPath(CARRIER.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toIllustrationsCode(String workBase) {
    return join(".", workBase, arrayPath(ILLUSTRATIONS.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toIllustrationsLink(String workBase) {
    return join(".", workBase, arrayPath(ILLUSTRATIONS.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toIllustrationsTerm(String workBase) {
    return join(".", workBase, arrayPath(ILLUSTRATIONS.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toCopyrightDate() {
    return join(".", toInstance(), arrayPath(COPYRIGHT.getUri()), arrayPath(DATE.getValue()));
  }

  public static String toMediaCode() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toMediaLink() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toMediaTerm() {
    return join(".", toInstance(), arrayPath(MEDIA.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toWorkTableOfContents(String workBase) {
    return join(".", workBase, arrayPath(TABLE_OF_CONTENTS.getValue()));
  }

  public static String toWorkSummary(String workBase) {
    return join(".", workBase, arrayPath(SUMMARY.getValue()));
  }

  public static String toLanguageCode(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toLanguageTerm(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toLanguageLink(String workBase) {
    return join(".", workBase, arrayPath(LANGUAGE.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toClassificationSources(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(SOURCE.getValue()));
  }

  public static String toClassificationCodes(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toClassificationItemNumbers(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(ITEM_NUMBER.getValue()));
  }

  public static String toWorkDeweyEditionNumber(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(EDITION_NUMBER.getValue()));
  }

  public static String toWorkDeweyEdition(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(EDITION.getValue()));
  }

  public static String toLcStatusValue(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LABEL.getValue()));
  }

  public static String toLcStatusLink(String workBase) {
    return join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri()), arrayPath(STATUS.getUri()),
      arrayPath(LINK.getValue()));
  }

  public static String toClassificationAssigningSourceIds(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri())),
      dynamicArrayPath(ASSIGNING_SOURCE_REF), path(ID_PROPERTY));
  }

  public static String toClassificationAssigningSourceLabels(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(CLASSIFICATION.getUri())),
      dynamicArrayPath(ASSIGNING_SOURCE_REF), path(LABEL_PROPERTY));
  }

  public static String toDissertationLabel(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(LABEL.getValue()));
  }

  public static String toDissertationDegree(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DEGREE.getValue()));
  }

  public static String toDissertationYear(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_YEAR.getValue()));
  }

  public static String toDissertationNote(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_NOTE.getValue()));
  }

  public static String toDissertationId(String workBase) {
    return join(".", workBase, arrayPath(DISSERTATION.getUri()), arrayPath(DISSERTATION_ID.getValue()));
  }

  public static String toDissertationGrantingInstitutionIds(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(DISSERTATION.getUri())),
      dynamicArrayPath(GRANTING_INSTITUTION_REF), path(ID_PROPERTY));
  }

  public static String toDissertationGrantingInstitutionLabels(String workBase) {
    return join(".", join(".", workBase, dynamicArrayPath(DISSERTATION.getUri())),
      dynamicArrayPath(GRANTING_INSTITUTION_REF), path(LABEL_PROPERTY));
  }

  public static String toWorkContentTerm(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toWorkSubjectLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(SUBJECT.getUri()), path(LABEL_PROPERTY));
  }

  public static String toWorkSubjectIsPreferred(String workBase) {
    return join(".", workBase, dynamicArrayPath(SUBJECT.getUri()), path(IS_PREFERRED_PROPERTY));
  }

  public static String toWorkGeographicCoverageLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(GEOGRAPHIC_COVERAGE_REF), path("label"));
  }

  public static String toWorkGenreLabel(String workBase) {
    return join(".", workBase, dynamicArrayPath(GENRE_REF), path(LABEL_PROPERTY));
  }

  public static String toWorkGenreIsPreferred(String workBase) {
    return join(".", workBase, dynamicArrayPath(GENRE_REF), path(IS_PREFERRED_PROPERTY));
  }

  public static String toWorkDateStart(String workBase) {
    return join(".", workBase, arrayPath(DATE_START.getValue()));
  }

  public static String toWorkDateEnd(String workBase) {
    return join(".", workBase, arrayPath(DATE_END.getValue()));
  }

  public static String toWorkGovPublicationCode(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toWorkGovPublicationTerm(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(TERM.getValue()));
  }

  public static String toWorkGovPublicationLink(String workBase) {
    return join(".", workBase, arrayPath(GOVERNMENT_PUBLICATION.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toWorkContentCode(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(CODE.getValue()));
  }

  public static String toWorkContentLink(String workBase) {
    return join(".", workBase, arrayPath(CONTENT.getUri()), arrayPath(LINK.getValue()));
  }

  public static String toId(String base) {
    return join(".", base, path("id"));
  }

  public static String path(String path) {
    return "['%s']".formatted(path);
  }

  public static String arrayPath(String path) {
    return "['%s'][0]".formatted(path);
  }

  public static String dynamicArrayPath(String path) {
    return "['%s'][*]".formatted(path);
  }
}
