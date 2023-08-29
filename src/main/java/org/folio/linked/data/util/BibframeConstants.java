package org.folio.linked.data.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BibframeConstants {

  // Types
  public static final String MONOGRAPH = "http://bibfra.me/vocab/marc/Monograph";
  public static final String INSTANCE = "http://bibfra.me/vocab/lite/Instance";
  public static final String ITEM = "http://bibfra.me/vocab/lite/Item";
  public static final String WORK = "http://bibfra.me/vocab/lite/Work";
  public static final String INSTANCE_TITLE = "http://bibfra.me/vocab/marc/Title";
  public static final String PARALLEL_TITLE = "http://bibfra.me/vocab/marc/ParallelTitle";
  public static final String VARIANT_TITLE = "http://bibfra.me/vocab/marc/VariantTitle";

  // Predicates
  public static final String INSTANCE_TITLE_PRED = "http://bibfra.me/vocab/bf/title";

  // Properties
  public static final String NOTE = "http://bibfra.me/vocab/lite/note";
  public static final String PART_NAME = "http://bibfra.me/vocab/marc/partName";
  public static final String PART_NUMBER = "http://bibfra.me/vocab/marc/partNumber";
  public static final String MAIN_TITLE = "http://bibfra.me/vocab/marc/mainTitle";
  public static final String DATE = "http://bibfra.me/vocab/lite/date";
  public static final String SUBTITLE = "http://bibfra.me/vocab/marc/subTitle";
  public static final String NON_SORT_NUM = "http://bibfra.me/vocab/bflc/nonSortNum";
  public static final String VARIANT_TYPE = "http://bibfra.me/vocab/marc/variantType";
  public static final String RESPONSIBILITY_STATEMENT = "http://bibfra.me/vocab/marc/statementOfResponsibility";
  public static final String EDITION_STATEMENT = "http://bibfra.me/vocab/marc/edition";
  public static final String COPYRIGHT_DATE = "http://bibfra.me/vocab/lite/copyrightDate";
  public static final String DIMENSIONS = "http://bibfra.me/vocab/lite/dimensions";
  public static final String PROJECTED_PROVISION_DATE = "http://bibfra.me/vocab/marc/projectedProvisionDate";
  public static final String MEDIA = "http://bibfra.me/vocab/marc/mediaType";
  public static final String CARRIER = "http://bibfra.me/vocab/marc/carrier";

  // fields
  public static final String ID = "id";
  public static final String TYPE = "type";

  // ============================= Bibframe 2.0, to be removed =========================
  public static final String PROPERTY_URI = "uri";
  public static final String PROPERTY_ID = "id";
  public static final String PROPERTY_LABEL = "label";

  //Types
  public static final String MONOGRAPH_2 = "lc:profile:bf2:Monograph";
  public static final String WORK_2 = "lc:RT:bf2:Monograph:Work";
  public static final String INSTANCE_2 = "c:RT:bf2:Monograph:Instance";
  public static final String ITEM_2 = "lc:RT:bf2:Monograph:Item";
  public static final String LCC = "lc:RT:bf2:LCC";
  public static final String DDC = "lc:RT:bf2:DDC";
  public static final String NLM = "lc:RT:bf2:NLM";
  public static final String OTHER_CLASS = "lc:RT:bf2:OtherClass";
  public static final String PRIMARY_CONTRIBUTION = "lc:RT:bflc:Agents:PrimaryContribution";
  public static final String CONTRIBUTION = "lc:RT:bf2:Agents:Contribution";
  public static final String NOTE_2 = "lc:RT:bf2:Note";
  public static final String REL_WORK_LOOKUP = "lc:RT:bf2:RelWorkLookup";
  public static final String COMPONENTS = "lc:RT:bf2:Components";
  public static final String PLACE_COMPONENTS = "lc:RT:bf2:Topic:Place:Components";
  public static final String CHILDRENS_COMPONENTS = "lc:RT:bf2:Topic:Childrens:Components";
  public static final String SUBJECT_WORK = "lc:RT:bf2:Topic:SubjectWork";
  public static final String WORK_TITLE = "lc:RT:bf2:WorkTitle";
  public static final String VARIANT_TITLE_2 = "lc:RT:bf2:Title:VarTitle";
  public static final String PARALLEL_TITLE_2 = "lc:RT:bf2:ParallelTitle";
  public static final String ITEM_ACCESS = "lc:RT:bf2:Item:Access";
  public static final String ITEM_USE = "lc:RT:bf2:Item:Use";
  public static final String ITEM_RETENTION = "lc:RT:bf2:Item:Retention";
  public static final String INSTANCE_TITLE_2 = "lc:RT:bf2:InstanceTitle";
  public static final String PUBLICATION = "lc:RT:bf2:PubInfoNew";
  public static final String DISTRIBUTION = "lc:RT:bf2:DistInfoNew";
  public static final String MANUFACTURE = "lc:RT:bf2:ManuInfoNew";
  public static final String PRODUCTION = "lc:RT:bf2:ProdInfoNew";
  public static final String IDENTIFIERS_LCCN = "lc:RT:bf2:Identifiers:LCCN";
  public static final String IDENTIFIERS_ISBN = "lc:RT:bf2:Identifiers:ISBN";
  public static final String IDENTIFIERS_OTHER = "lc:RT:bf2:Identifiers:Other";
  public static final String IDENTIFIERS_LOCAL = "lc:RT:bf2:Identifiers:Local";
  public static final String IDENTIFIERS_EAN = "lc:RT:bf2:Identifiers:EAN";
  public static final String ORGANIZATION = "lc:RT:bf2:Agent:bfCorp";
  public static final String EXTENT = "lc:RT:bf2:Extent";
  public static final String PERSON = "lc:RT:bf2:Agent:bfPerson";
  public static final String FAMILY = "lc:RT:bf2:Agent:bfFamily";
  public static final String JURISDICTION = "lc:RT:bf2:Agent:bfJurisdiction";
  public static final String MEETING = "lc:RT:bf2:Agent:bfConf";
  public static final String ROLE = "lc:RT:bf2:Agent:bfRole";
  public static final String PLACE = "lc:RT:bf2:Place";
  public static final String URL = "lc:RT:bf2:URL";
  public static final String IMM_ACQUISITION = "lc:RT:bf2:ImmAcqSource";
  public static final String SUPP_CONTENT = "lc:RT:bf2:SupplContentNote";
  public static final String APPLIES_TO = "lc:RT:bf2:AppliesTo";

  //Type URLs
  public static final String WORK_URL = "http://id.loc.gov/ontologies/bibframe/Work";
  public static final String INSTANCE_URL = "http://id.loc.gov/ontologies/bibframe/Instance";
  public static final String ITEM_URL = "http://id.loc.gov/ontologies/bibframe/Item";
  public static final String CONTRIBUTION_URL = "http://id.loc.gov/ontologies/bibframe/Contribution";
  public static final String NOTE_URL = "http://id.loc.gov/ontologies/bibframe/Note";
  public static final String PUBLICATION_URL = "http://id.loc.gov/ontologies/bibframe/Publication";
  public static final String EXTENT_URL = "http://id.loc.gov/ontologies/bibframe/Extent";
  public static final String ISSUANCE_URL = "http://id.loc.gov/ontologies/bibframe/Issuance";
  public static final String ORGANIZATION_URL = "http://id.loc.gov/ontologies/bibframe/Organization";
  public static final String PART_NAME_URL = "http://id.loc.gov/ontologies/bibframe/partName";
  public static final String PART_NUMBER_URL = "http://id.loc.gov/ontologies/bibframe/partNumber";
  public static final String MAIN_TITLE_URL = "http://id.loc.gov/ontologies/bibframe/mainTitle";
  public static final String DATE_URL = "http://id.loc.gov/ontologies/bibframe/date";
  public static final String SUBTITLE_URL = "http://id.loc.gov/ontologies/bibframe/subtitle";
  public static final String NON_SORT_NUM_URL = "http://id.loc.gov/ontologies/bflc/nonSortNum";
  public static final String VARIANT_TYPE_URL = "http://id.loc.gov/ontologies/bibframe/variantType";
  public static final String QUALIFIER_URL = "http://id.loc.gov/ontologies/bibframe/qualifier";
  public static final String INSTANCE_TITLE_URL = "http://id.loc.gov/ontologies/bibframe/Title";
  public static final String PARALLEL_TITLE_URL = "http://id.loc.gov/ontologies/bibframe/ParallelTitle";
  public static final String VARIANT_TITLE_URL = "http://id.loc.gov/ontologies/bibframe/VariantTitle";
  public static final String IDENTIFIERS_LCCN_URL = "http://id.loc.gov/ontologies/bibframe/Lccn";
  public static final String IDENTIFIERS_EAN_URL = "http://id.loc.gov/ontologies/bibframe/Ean";
  public static final String IDENTIFIERS_ISBN_URL = "http://id.loc.gov/ontologies/bibframe/Isbn";
  public static final String IDENTIFIERS_OTHER_URL = "http://id.loc.gov/ontologies/bibframe/Identifier";
  public static final String IDENTIFIERS_LOCAL_URL = "http://id.loc.gov/ontologies/bibframe/Local";
  public static final String PLACE_URL = "http://id.loc.gov/ontologies/bibframe/Place";
  public static final String DISTRIBUTION_URL = "http://id.loc.gov/ontologies/bibframe/Distribution";
  public static final String MANUFACTURE_URL = "http://id.loc.gov/ontologies/bibframe/Manufacture";
  public static final String PRODUCTION_URL = "http://id.loc.gov/ontologies/bibframe/Production";
  public static final String IMM_ACQUISITION_URI = "http://id.loc.gov/ontologies/bibframe/ImmediateAcquisition";
  public static final String URL_URL = "http://www.w3.org/2000/01/rdf-schema#Resource";

  //Property type URLs
  public static final String CARRIER_URL = "http://id.loc.gov/ontologies/bibframe/Carrier";
  public static final String MEDIA_URL = "http://id.loc.gov/ontologies/bibframe/Media";
  public static final String PERSON_URL = "http://id.loc.gov/ontologies/bibframe/Person";
  public static final String FAMILY_URL = "http://id.loc.gov/ontologies/bibframe/Family";
  public static final String JURISDICTION_URL = "http://id.loc.gov/ontologies/bibframe/Jurisdiction";
  public static final String MEETING_URL = "http://id.loc.gov/ontologies/bibframe/Meeting";

  //Property type URLs
  public static final String ROLE_URL = "http://id.loc.gov/ontologies/bibframe/Role";
  public static final String STATUS_URL = "http://id.loc.gov/ontologies/bibframe/Status";
  public static final String SUPP_CONTENT_URL = "http://id.loc.gov/ontologies/bibframe/SupplementaryContent";
  public static final String APPLICABLE_INSTITUTION_URL = "http://id.loc.gov/ontologies/bflc/ApplicableInstitution";
  public static final String APPLIES_TO_URL = "http://id.loc.gov/ontologies/bflc/AppliesTo";
  public static final String ASSIGNER_URL = "http://id.loc.gov/ontologies/bibframe/Assigner";
  public static final String NOTE_TYPE_URI = "http://id.loc.gov/ontologies/bibframe/NoteType";

  //Literal URLs
  public static final String RESPONSIBILITY_STATEMENT_URL =
    "http://id.loc.gov/ontologies/bibframe/responsibilityStatement";
  public static final String EDITION_STATEMENT_URL = "http://id.loc.gov/ontologies/bibframe/editionStatement";
  public static final String COPYRIGHT_DATE_URL = "http://id.loc.gov/ontologies/bibframe/copyrightDate";
  public static final String DIMENSIONS_URL = "http://id.loc.gov/ontologies/bibframe/dimensions";
  public static final String PROJECTED_PROVISION_DATE_URL = "http://id.loc.gov/ontologies/bflc/projectedProvisionDate";

  //Predicates
  public static final String CLASSIFICATION_PRED = "http://id.loc.gov/ontologies/bibframe/classification";
  public static final String CONTRIBUTION_PRED = "http://id.loc.gov/ontologies/bibframe/contribution";
  public static final String COLOR_CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/colorContent";
  public static final String CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/content";
  public static final String TABLE_OF_CONTENTS_PRED = "http://id.loc.gov/ontologies/bibframe/tableOfContents";
  public static final String DISSERTATION_PRED = "http://id.loc.gov/ontologies/bibframe/dissertation";
  public static final String GENRE_FORM_PRED = "http://id.loc.gov/ontologies/bibframe/genreForm";
  public static final String GEOGRAPHIC_COVERAGE_PRED = "http://id.loc.gov/ontologies/bibframe/geographicCoverage";
  public static final String GOVERNMENT_PUB_TYPE_PRED = "http://id.loc.gov/ontologies/bflc/governmentPubType";
  public static final String ILLUSTRATIVE_CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/illustrativeContent";
  public static final String INTENDED_AUDIENCE_PRED = "http://id.loc.gov/ontologies/bibframe/intendedAudience";
  public static final String LANGUAGE_PRED = "http://id.loc.gov/ontologies/bibframe/language";
  public static final String NOTE_PRED = "http://id.loc.gov/ontologies/bibframe/note";
  public static final String ORIGIN_PLACE_PRED = "http://id.loc.gov/ontologies/bibframe/originPlace";
  public static final String RELATIONSHIP_PRED = "http://id.loc.gov/ontologies/bflc/relationship";
  public static final String SUBJECT_PRED = "http://id.loc.gov/ontologies/bibframe/subject";
  public static final String SUMMARY_PRED = "http://id.loc.gov/ontologies/bibframe/summary";
  public static final String SUPP_CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/supplementaryContent";
  public static final String INSTANCE_TITLE_2_PRED = "http://id.loc.gov/ontologies/bibframe/title";
  public static final String OTHER_EDITION_PRED = "http://id.loc.gov/ontologies/bibframe/otherEdition";
  public static final String OTHER_PHYSICAL_FORMAT_PRED = "http://id.loc.gov/ontologies/bibframe/otherPhysicalFormat";
  public static final String PROVISION_ACTIVITY_PRED = "http://id.loc.gov/ontologies/bibframe/provisionActivity";
  public static final String IDENTIFIED_BY_PRED = "http://id.loc.gov/ontologies/bibframe/identifiedBy";
  public static final String IMM_ACQUISITION_PRED = "http://id.loc.gov/ontologies/bibframe/immediateAcquisition";
  public static final String EXTENT_PRED = "http://id.loc.gov/ontologies/bibframe/extent";
  public static final String ELECTRONIC_LOCATOR_PRED = "http://id.loc.gov/ontologies/bibframe/electronicLocator";
  public static final String DATE_PRED = "http://id.loc.gov/ontologies/bibframe/date";
  public static final String ISSUANCE_PRED = "http://id.loc.gov/ontologies/bibframe/issuance";
  public static final String MEDIA_PRED = "http://id.loc.gov/ontologies/bibframe/media";
  public static final String CARRIER_PRED = "http://id.loc.gov/ontologies/bibframe/carrier";
  public static final String AGENT_PRED = "http://id.loc.gov/ontologies/bibframe/agent";
  public static final String ROLE_PRED = "http://id.loc.gov/ontologies/bibframe/role";
  public static final String ENUMERATION_AND_CHRONOLOGY_PRED =
    "http://id.loc.gov/ontologies/bibframe/enumerationAndChronology";
  public static final String USAGE_AND_ACCESS_POLICY_PRED =
    "http://id.loc.gov/ontologies/bibframe/usageAndAccessPolicy";
  public static final String APPLICABLE_INSTITUTION_PRED =
    "http://id.loc.gov/ontologies/bflc/applicableInstitution";
  public static final String GRANTING_INSTITUTION_PRED = "http://id.loc.gov/ontologies/bibframe/grantingInstitution";
  public static final String SAME_AS_PRED = "http://www.w3.org/2002/07/owl#sameAs";
  public static final String STATUS_PRED = "http://id.loc.gov/ontologies/bibframe/status";
  public static final String ASSIGNER_PRED = "http://id.loc.gov/ontologies/bibframe/assigner";
  public static final String SOURCE_PRED = "http://id.loc.gov/ontologies/bibframe/source";
  public static final String COMPONENT_LIST_PRED = "http://www.loc.gov/mads/rdf/v1#componentList";
  public static final String PART_PRED = "http://id.loc.gov/ontologies/bibframe/part";
  public static final String RELATION_PRED = "http://id.loc.gov/ontologies/bflc/relation";
  public static final String RELATED_TO_PRED = "http://id.loc.gov/ontologies/bibframe/relatedTo";
  public static final String PLACE_PRED = "http://id.loc.gov/ontologies/bibframe/place";
  public static final String APPLIES_TO_PRED = "http://id.loc.gov/ontologies/bflc/appliesTo";
  public static final String MAIN_TITLE_PRED = "http://id.loc.gov/ontologies/bibframe/mainTitle";
  public static final String SIMPLE_PLACE_PRED = "http://id.loc.gov/ontologies/bflc/simplePlace";
  public static final String SIMPLE_AGENT_PRED = "http://id.loc.gov/ontologies/bflc/simpleAgent";
  public static final String SIMPLE_DATE_PRED = "http://id.loc.gov/ontologies/bflc/simpleDate";
  public static final String LABEL_PRED = "http://www.w3.org/2000/01/rdf-schema#label";
  public static final String VALUE_PRED = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value";
  public static final String NOTE_TYPE_PRED = "http://id.loc.gov/ontologies/bibframe/noteType";

  // fields
  public static final String PROFILE = "profile";
}
