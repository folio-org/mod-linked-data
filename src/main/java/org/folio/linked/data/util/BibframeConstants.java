package org.folio.linked.data.util;

public class BibframeConstants {

  //Types
  public static final String MONOGRAPH = "lc:profile:bf2:Monograph";
  public static final String WORK = "lc:RT:bf2:Monograph:Work";
  public static final String INSTANCE = "c:RT:bf2:Monograph:Instance";
  public static final String ITEM = "lc:RT:bf2:Monograph:Item";
  public static final String LCC = "lc:RT:bf2:LCC";
  public static final String DDC = "lc:RT:bf2:DDC";
  public static final String NLM = "lc:RT:bf2:NLM";
  public static final String OTHER_CLASS = "lc:RT:bf2:OtherClass";
  public static final String PRIMARY_CONTRIBUTION = "lc:RT:bflc:Agents:PrimaryContribution";
  public static final String CONTRIBUTION = "lc:RT:bf2:Agents:Contribution";
  public static final String NOTE = "lc:RT:bf2:Note";
  public static final String REL_WORK_LOOKUP = "lc:RT:bf2:RelWorkLookup";
  public static final String SUMMARY = "lc:RT:bf2:Summary";
  public static final String CONTENTS = "lc:RT:bf2:Contents";
  public static final String MONOGRAPH_DISSERTATION = "lc:RT:bf2:Monograph:Dissertation";
  public static final String FORM = "lc:RT:bf2:Form";
  public static final String GEOGRAPHIC_FORM = "lc:RT:bf2:Form:Geog";
  public static final String LANGUAGE = "lc:RT:bf2:Language2";
  public static final String COMPONENTS = "lc:RT:bf2:Components";
  public static final String PLACE_COMPONENTS = "lc:RT:bf2:Topic:Place:Components";
  public static final String CHILDRENS_COMPONENTS = "lc:RT:bf2:Topic:Childrens:Components";
  public static final String SUBJECT_WORK = "lc:RT:bf2:SubjectWork";
  public static final String WORK_TITLE = "lc:RT:bf2:WorkTitle";
  public static final String VARIANT_TITLE = "lc:RT:bf2:Title:VarTitle";
  public static final String PARALLEL_TITLE = "lc:RT:bf2:ParallelTitle";
  public static final String SERIES_HUB = "lc:RT:bf2:SeriesHub";
  public static final String RELATED_WORK_INPUT = "lc:RT:bf2:RelWorkInput";
  public static final String SERIES_HUB_LOOKUP = "lc:RT:bf2:SeriesHubLookup";
  public static final String AGENTS_BF_CONTRIBUTION = "lc:RT:bf2:Agents:bfContribution";
  public static final String ITEM_ENUMERATION = "lc:RT:bf2:Item:Enumeration";
  public static final String ITEM_CHRONOLOGY = "lc:RT:bf2:Item:Chronology";
  public static final String ITEM_ACCESS = "lc:RT:bf2:Item:Access";
  public static final String ITEM_USE = "lc:RT:bf2:Item:Use";
  public static final String ITEM_RETENTION = "lc:RT:bf2:Item:Retention";
  public static final String NOTE2 = "lc:RT:bf2:Note";
  public static final String ITEM_LCC = "lc:RT:bf2:Item:LCC";
  public static final String URL = "lc:RT:bf2:URL";
  public static final String AGENT_BF_CORP = "lc:RT:bf2:Agent:bfCorp";
  public static final String NOTE_SIMPLE = "lc:RT:bf2:NoteSimple";
  public static final String INSTANCE_TITLE = "lc:RT:bf2:InstanceTitle";
  public static final String PUBLICATION = "lc:RT:bf2:PubInfoNew";
  public static final String DISTRIBUTION = "lc:RT:bf2:DistInfoNew";
  public static final String MANUFACTURE = "lc:RT:bf2:ManuInfoNew";
  public static final String PRODUCTION = "lc:RT:bf2:ProdInfoNew";
  public static final String AGENTS_CONTRIBUTION = "lc:RT:bf2:Agents:Contribution";
  public static final String IDENTIFIERS_LCCN = "lc:RT:bf2:Identifiers:LCCN";
  public static final String IDENTIFIERS_ISBN = "lc:RT:bf2:Identifiers:ISBN";
  public static final String IDENTIFIERS_OTHER = "lc:RT:bf2:Identifiers:Other";
  public static final String IDENTIFIERS_LOCAL = "lc:RT:bf2:Identifiers:Local";
  public static final String IDENTIFIERS_EAN = "lc:RT:bf2:Identifiers:EAN";
  public static final String SUPPL_CONTENT_NOTE = "lc:RT:bf2:SupplContentNote";
  public static final String IMM_ACQ_SOURCE = "lc:RT:bf2:ImmAcqSource";
  public static final String EXTENT = "lc:RT:bf2:Extent";
  public static final String MONOGRAPH_ITEM = "lc:RT:bf2:Monograph:Item";

  //Type URLs
  public static final String MONOGRAPH_URL = "http://id.loc.gov/ontologies/bflc/Monograph";
  public static final String WORK_URL = "http://id.loc.gov/ontologies/bibframe/Work";
  public static final String INSTANCE_URL = "http://id.loc.gov/ontologies/bibframe/Work";
  public static final String ITEM_URL = "http://id.loc.gov/ontologies/bibframe/Work";


  //Predicates
  public static final String HAS_INSTANCE_PRED = "http://id.loc.gov/ontologies/bibframe/hasInstance";
  public static final String CLASSIFICATION_PRED = "http://id.loc.gov/ontologies/bibframe/classification";
  public static final String CONTRIBUTION_PRED = "http://id.loc.gov/ontologies/bibframe/contribution";
  public static final String COLOR_CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/colorContent";
  public static final String CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/content";
  public static final String TABLE_OF_CONTENTS_PRED = "http://id.loc.gov/ontologies/bibframe/tableOfContents";
  public static final String ORIGIN_DATE_PRED = "http://id.loc.gov/ontologies/bibframe/originDate";
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
  public static final String SUPPLEMENTARY_CONTENT_PRED = "http://id.loc.gov/ontologies/bibframe/supplementaryContent";
  public static final String TEMPORAL_COVERAGE_PRED = "http://id.loc.gov/ontologies/bibframe/temporalCoverage";
  public static final String TITLE_PRED = "http://id.loc.gov/ontologies/bibframe/title";
  public static final String OTHER_EDITION_PRED = "http://id.loc.gov/ontologies/bibframe/otherEdition";
  public static final String OTHER_PHYSICAL_FORMAT_PRED = "http://id.loc.gov/ontologies/bibframe/otherPhysicalFormat";
  public static final String INSTANCE_OF_PRED = "http://id.loc.gov/ontologies/bibframe/InstanceOf";
  public static final String HAS_ITEM_PRED = "http://id.loc.gov/ontologies/bibframe/hasItem";
  public static final String PROVISION_ACTIVITY_PRED = "http://id.loc.gov/ontologies/bibframe/provisionActivity";
  public static final String IDENTIFIED_BY_PRED = "http://id.loc.gov/ontologies/bibframe/identifiedBy";
  public static final String IMMEDIATE_ACQUISITION_PRED = "http://id.loc.gov/ontologies/bibframe/immediateAcquisition";
  public static final String EXTENT_PRED = "http://id.loc.gov/ontologies/bibframe/extent";
  public static final String ELECTRONIC_LOCATOR_PRED = "http://id.loc.gov/ontologies/bibframe/electronicLocator";
  public static final String RESPONSIBILITY_STATEMENT_PRED = "http://id.loc.gov/ontologies/bibframe/responsiblityStatement";
  public static final String EDITION_STATEMENT_PRED = "http://id.loc.gov/ontologies/bibframe/editionStatement";
  public static final String DATE_PRED = "http://id.loc.gov/ontologies/bibframe/date";
  public static final String COPYRIGHT_DATE_PRED = "http://id.loc.gov/ontologies/bibframe/copyrightDate";
  public static final String DIMENSIONS_PRED = "http://id.loc.gov/ontologies/bibframe/dimensions";
  public static final String PROJECT_PROVISION_DATE_PRED = "http://id.loc.gov/ontologies/bibframe/bflc/projectProvisionDate";
  public static final String ISSUANCE_PRED = "http://id.loc.gov/ontologies/bibframe/issuance";
  public static final String MEDIA_PRED = "http://id.loc.gov/ontologies/bibframe/media";
  public static final String CARRIER_PRED = "http://id.loc.gov/ontologies/bibframe/carrier";
  public static final String AGENT_PRED = "http://id.loc.gov/ontologies/bibframe/agent";
  public static final String ROLE_PRED = "http://id.loc.gov/ontologies/bibframe/role";
  public static final String ENUMERATION_AND_CHRONOLOGY_PRED = "http://id.loc.gov/ontologies/bibframe/enumerationAndChronology";
  public static final String USAGE_AND_ACCESS_POLICY_PRED = "http://id.loc.gov/ontologies/bibframe/usageAndAccessPolicy";
  public static final String APPLICABLE_INSTITUTION_PRED = "http://id.loc.gov/ontologies/bibframe/applicableInstitution";
  public static final String GRANTING_INSTITUTION_PRED = "http://id.loc.gov/ontologies/bibframe/grantingInstitution";
  public static final String CLASSIFICATION_PORTION_PRED = "http://id.loc.gov/ontologies/bibframe/classificationPortion";
  public static final String ITEM_PORTION_PRED = "http://id.loc.gov/ontologies/bibframe/itemPortion";
  public static final String SAME_AS_PRED = "http://www.w3.org/2002/07/owl#sameAs";
  public static final String STATUS_PRED = "http://id.loc.gov/ontologies/bibframe/status";
  public static final String ASSIGNER_PRED = "http://id.loc.gov/ontologies/bibframe/assigner";
  public static final String SOURCE_PRED = "http://id.loc.gov/ontologies/bibframe/source";
  public static final String PART_PRED = "http://id.loc.gov/ontologies/bibframe/part";
  public static final String RELATION_PRED = "http://id.loc.gov/ontologies/bflc/relation";
  public static final String RELATED_TO_PRED = "http://id.loc.gov/ontologies/bibframe/relatedTo";
  public static final String PLACE_PRED = "http://id.loc.gov/ontologies/bibframe/place";
  public static final String APPLIES_TO = "http://id.loc.gov/ontologies/bflc/appliesTo";


}
