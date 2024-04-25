package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNEE;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.EDITOR;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGIN_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ADDITIONAL_PHYSICAL_FORM;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.BIBLIOGRAPHY_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.COMPUTER_DATA_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_END;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_START;
import static org.folio.ld.dictionary.PropertyDictionary.DESCRIPTION_SOURCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.EXHIBITIONS_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.FUNDING_INFORMATION;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_AREA_CODE;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUING_BODY;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.LCNAF_ID;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCATION_OF_OTHER_ARCHIVAL_MATERIAL;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.ORIGINAL_VERSION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.PropertyDictionary.RELATED_PARTS;
import static org.folio.ld.dictionary.PropertyDictionary.REPRODUCTION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.TYPE_OF_REPORT;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.PropertyDictionary.WITH_NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;

@UtilityClass
public class MonographTestUtil {

  public static Resource getSampleInstanceResource() {
    return getSampleInstanceResource(null, getSampleWork(null));
  }

  public static Resource getSampleInstanceResource(Long id) {
    return getSampleInstanceResource(id, getSampleWork(null));
  }

  public static Resource getSampleInstanceResource(Long id, Resource linkedWork) {
    var basicTitle = createBasicTitle(id);

    var production = providerEvent("production");
    var publication = providerEvent("publication");
    var distribution = providerEvent("distribution");
    var manufacture = providerEvent("manufacture");

    var supplementaryContent = createResource(
      Map.of(
        LINK, List.of("supplementaryContent link"),
        NAME, List.of("supplementaryContent name")
      ),
      Set.of(SUPPLEMENTARY_CONTENT),
      emptyMap()
    ).setLabel("supplementaryContent name");

    var accessLocation = createResource(
      Map.of(
        LINK, List.of("accessLocation value"),
        NOTE, List.of("accessLocation note")
      ),
      Set.of(ANNOTATION),
      emptyMap()
    ).setLabel("accessLocation value");

    var lccn = createResource(
      Map.of(NAME, List.of("lccn value")),
      Set.of(IDENTIFIER, ID_LCCN),
      Map.of(STATUS, List.of(status("lccn")))
    ).setLabel("lccn value");

    var isbn = createResource(
      Map.of(
        NAME, List.of("isbn value"),
        QUALIFIER, List.of("isbn qualifier")
      ),
      Set.of(IDENTIFIER, ID_ISBN),
      Map.of(STATUS, List.of(status("isbn")))
    ).setLabel("isbn value");

    var ean = createResource(
      Map.of(
        EAN_VALUE, List.of("ean value"),
        QUALIFIER, List.of("ean qualifier")
      ),
      Set.of(IDENTIFIER, ID_EAN),
      emptyMap()
    ).setLabel("ean value");

    var localId = createResource(
      Map.of(
        LOCAL_ID_VALUE, List.of("localId value"),
        ASSIGNING_SOURCE, List.of("localId assigner")
      ),
      Set.of(IDENTIFIER, ID_LOCAL),
      emptyMap()
    ).setLabel("localId value");

    var otherId = createResource(
      Map.of(
        NAME, List.of("otherId value"),
        QUALIFIER, List.of("otherId qualifier")
      ),
      Set.of(IDENTIFIER, ID_UNKNOWN),
      emptyMap()
    ).setLabel("otherId value");

    var media = createResource(
      Map.of(
        CODE, List.of("s"),
        TERM, List.of("media term"),
        LINK, List.of("http://id.loc.gov/vocabulary/mediaTypes/s"),
        SOURCE, List.of("media source")
      ),
      Set.of(CATEGORY),
      emptyMap()
    ).setLabel("media term");

    var carrier = createResource(
      Map.of(
        CODE, List.of("ha"),
        TERM, List.of("carrier term"),
        LINK, List.of("http://id.loc.gov/vocabulary/carriers/ha"),
        SOURCE, List.of("carrier source")
      ),
      Set.of(CATEGORY),
      emptyMap()
    ).setLabel("carrier term");

    var copyrightEvent = createResource(
      Map.of(
        DATE, List.of("copyright date value")
      ),
      Set.of(COPYRIGHT_EVENT),
      emptyMap()
    ).setLabel("copyright date value");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(basicTitle, createParallelTitle(), createVariantTitle()));
    pred2OutgoingResources.put(PE_PRODUCTION, List.of(production));
    pred2OutgoingResources.put(PE_PUBLICATION, List.of(publication));
    pred2OutgoingResources.put(PE_DISTRIBUTION, List.of(distribution));
    pred2OutgoingResources.put(PE_MANUFACTURE, List.of(manufacture));
    pred2OutgoingResources.put(PredicateDictionary.SUPPLEMENTARY_CONTENT, List.of(supplementaryContent));
    pred2OutgoingResources.put(ACCESS_LOCATION, List.of(accessLocation));
    pred2OutgoingResources.put(MAP, List.of(lccn, isbn, ean, localId, otherId));
    pred2OutgoingResources.put(MEDIA, List.of(media));
    pred2OutgoingResources.put(CARRIER, List.of(carrier));
    pred2OutgoingResources.put(COPYRIGHT, List.of(copyrightEvent));

    var instance = createResource(
      Map.ofEntries(
        entry(EXTENT, List.of("extent info")),
        entry(DIMENSIONS, List.of("20 cm")),
        entry(EDITION_STATEMENT, List.of("edition statement")),
        entry(PROJECTED_PROVISION_DATE, List.of("projected provision date")),
        entry(ISSUANCE, List.of("single unit")),
        entry(ADDITIONAL_PHYSICAL_FORM, List.of("additional physical form")),
        entry(COMPUTER_DATA_NOTE, List.of("computer data note")),
        entry(DESCRIPTION_SOURCE_NOTE, List.of("description source note")),
        entry(EXHIBITIONS_NOTE, List.of("exhibitions note")),
        entry(FUNDING_INFORMATION, List.of("funding information")),
        entry(ISSUANCE_NOTE, List.of("issuance note")),
        entry(ISSUING_BODY, List.of("issuing body")),
        entry(LOCATION_OF_OTHER_ARCHIVAL_MATERIAL, List.of("location of other archival material")),
        entry(NOTE, List.of("note")),
        entry(ORIGINAL_VERSION_NOTE, List.of("original version note")),
        entry(RELATED_PARTS, List.of("related parts")),
        entry(REPRODUCTION_NOTE, List.of("reproduction note")),
        entry(TYPE_OF_REPORT, List.of("type of report")),
        entry(WITH_NOTE, List.of("with note"))
      ),
      Set.of(INSTANCE),
      pred2OutgoingResources)
      .setInventoryId(UUID.fromString("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1"))
      .setSrsId(UUID.fromString("43d58061-decf-4d74-9747-0e1c368e861b"));

    if (nonNull(id)) {
      instance.setId(id);
    }
    instance.setLabel(basicTitle.getLabel());
    if (nonNull(linkedWork)) {
      var edge = new ResourceEdge(instance, linkedWork, INSTANTIATES);
      instance.addOutgoingEdge(edge);
      linkedWork.addIncomingEdge(edge);
    }
    return instance;
  }

  private static Resource createBasicTitle(Long id) {
    var basicTitleValue = "Basic: mainTitle" + (nonNull(id) ? id : "");

    return createResource(
      Map.of(
        PART_NAME, List.of("Basic: partName"),
        PART_NUMBER, List.of("Basic: partNumber"),
        MAIN_TITLE, List.of(basicTitleValue),
        NON_SORT_NUM, List.of("Basic: nonSortNum"),
        SUBTITLE, List.of("Basic: subTitle")
      ),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel(basicTitleValue);
  }

  private static Resource createParallelTitle() {
    return createResource(
      Map.of(
        PART_NAME, List.of("Parallel: partName"),
        PART_NUMBER, List.of("Parallel: partNumber"),
        MAIN_TITLE, List.of("Parallel: mainTitle"),
        DATE, List.of("Parallel: date"),
        SUBTITLE, List.of("Parallel: subTitle"),
        NOTE, List.of("Parallel: noteLabel")
      ),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel("Parallel: mainTitle");
  }

  private static Resource createVariantTitle() {
    return createResource(
      Map.of(
        PART_NAME, List.of("Variant: partName"),
        PART_NUMBER, List.of("Variant: partNumber"),
        MAIN_TITLE, List.of("Variant: mainTitle"),
        DATE, List.of("Variant: date"),
        SUBTITLE, List.of("Variant: subTitle"),
        VARIANT_TYPE, List.of("Variant: variantType"),
        NOTE, List.of("Variant: noteLabel")
      ),
      Set.of(VARIANT_TITLE),
      emptyMap()
    ).setLabel("Variant: mainTitle");
  }

  public static Resource getSampleWork(Resource linkedInstance) {
    var basicTitle = createBasicTitle(null);

    var deweyClassification = createResource(
      Map.of(
        CODE, List.of("709.83"),
        SOURCE, List.of("ddc")
      ),
      Set.of(CATEGORY),
      emptyMap()
    ).setLabel("709.83");

    var creatorMeeting = createResource(
      Map.of(
        NAME, List.of("name-MEETING"),
        LCNAF_ID, List.of("2002801801-MEETING")
      ),
      Set.of(MEETING),
      emptyMap()
    ).setLabel("name-MEETING")
      .setId(1001L);

    var creatorPerson = createResource(
      Map.of(
        NAME, List.of("name-PERSON"),
        LCNAF_ID, List.of("2002801801-PERSON")
      ),
      Set.of(PERSON),
      emptyMap()
    ).setLabel("name-PERSON")
      .setId(1002L);

    var creatorOrganization = createResource(
      Map.of(
        NAME, List.of("name-ORGANIZATION"),
        LCNAF_ID, List.of("2002801801-ORGANIZATION")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("name-ORGANIZATION")
      .setId(1003L);

    var creatorFamily = createResource(
      Map.of(
        NAME, List.of("name-FAMILY"),
        LCNAF_ID, List.of("2002801801-FAMILY")
      ),
      Set.of(FAMILY),
      emptyMap()
    ).setLabel("name-FAMILY")
      .setId(1004L);

    var contributorPerson = createResource(
      Map.of(
        NAME, List.of("name-PERSON"),
        LCNAF_ID, List.of("2002801801-PERSON")
      ),
      Set.of(PERSON),
      emptyMap()
    ).setLabel("name-PERSON")
      .setId(1007L);

    var contributorMeeting = createResource(
      Map.of(
        NAME, List.of("name-MEETING"),
        LCNAF_ID, List.of("2002801801-MEETING")
      ),
      Set.of(MEETING),
      emptyMap()
    ).setLabel("name-MEETING")
      .setId(1008L);

    var contributorOrganization = createResource(
      Map.of(
        NAME, List.of("name-ORGANIZATION"),
        LCNAF_ID, List.of("2002801801-ORGANIZATION")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("name-ORGANIZATION")
      .setId(1005L);

    var contributorFamily = createResource(
      Map.of(
        NAME, List.of("name-FAMILY"),
        LCNAF_ID, List.of("2002801801-FAMILY")
      ),
      Set.of(FAMILY),
      emptyMap()
    ).setLabel("name-FAMILY")
      .setId(1006L);

    var subject1 = createResource(
      Map.of(
        NAME, List.of("Subject 1")
      ),
      Set.of(CONCEPT),
      emptyMap()
    ).setLabel("subject 1")
      .setId(1L);

    var subject2 = createResource(
      Map.of(
        NAME, List.of("Subject 2")
      ),
      Set.of(CONCEPT),
      emptyMap()
    ).setLabel("subject 2")
      .setId(2L);

    var unitedStates = createResource(
      Map.of(
        NAME, List.of("United States"),
        GEOGRAPHIC_AREA_CODE, List.of("n-us"),
        GEOGRAPHIC_COVERAGE, List.of("https://id.loc.gov/vocabulary/geographicAreas/n-us")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("United States")
      .setId(101L);

    var europe = createResource(
      Map.of(
        NAME, List.of("Europe"),
        GEOGRAPHIC_AREA_CODE, List.of("e"),
        GEOGRAPHIC_COVERAGE, List.of("https://id.loc.gov/vocabulary/geographicAreas/e")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("Europe")
      .setId(102L);

    var genre1 = createResource(
      Map.of(
        NAME, List.of("genre 1")
      ),
      Set.of(FORM),
      emptyMap()
    ).setLabel("genre 1")
      .setId(201L);

    var genre2 = createResource(
      Map.of(
        NAME, List.of("genre 2")
      ),
      Set.of(FORM),
      emptyMap()
    ).setLabel("genre 2")
      .setId(202L);

    var governmentPublication = createResource(
      Map.of(
        CODE, List.of("a"),
        TERM, List.of("Autonomous"),
        LINK, List.of("http://id.loc.gov/vocabulary/mgovtpubtype/a")
      ),
      Set.of(CATEGORY),
      emptyMap()
    ).setLabel("Autonomous");

    var originPlace = createResource(
      Map.of(
        NAME, List.of("France"),
        CODE, List.of("code"),
        LINK, List.of("link")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("France");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(basicTitle, createParallelTitle(), createVariantTitle()));
    pred2OutgoingResources.put(CLASSIFICATION, List.of(deweyClassification));
    pred2OutgoingResources.put(CREATOR, List.of(creatorPerson, creatorMeeting, creatorOrganization, creatorFamily));
    pred2OutgoingResources.put(AUTHOR, List.of(creatorPerson));
    pred2OutgoingResources.put(CONTRIBUTOR, List.of(contributorPerson, contributorMeeting, contributorOrganization,
      contributorFamily));
    pred2OutgoingResources.put(EDITOR, List.of(contributorOrganization));
    pred2OutgoingResources.put(ASSIGNEE, List.of(contributorOrganization));
    pred2OutgoingResources.put(CONTENT, List.of(createContent()));
    pred2OutgoingResources.put(SUBJECT, List.of(subject1, subject2));
    pred2OutgoingResources.put(PredicateDictionary.GEOGRAPHIC_COVERAGE, List.of(unitedStates, europe));
    pred2OutgoingResources.put(GENRE, List.of(genre1, genre2));
    pred2OutgoingResources.put(GOVERNMENT_PUBLICATION, List.of(governmentPublication));
    pred2OutgoingResources.put(ORIGIN_PLACE, List.of(originPlace));
    pred2OutgoingResources.put(TARGET_AUDIENCE, List.of(createTargetAudience()));

    var work = createResource(
      Map.ofEntries(
        entry(LANGUAGE, List.of("eng")),
        entry(SUMMARY, List.of("summary text")),
        entry(TABLE_OF_CONTENTS, List.of("table of contents")),
        entry(RESPONSIBILITY_STATEMENT, List.of("statement of responsibility")),
        entry(BIBLIOGRAPHY_NOTE, List.of("bibliography note")),
        entry(LANGUAGE_NOTE, List.of("language note", "another note")),
        entry(NOTE, List.of("note", "another note")),
        entry(DATE_START, List.of("2024")),
        entry(DATE_END, List.of("2025"))
      ),
      Set.of(WORK),
      pred2OutgoingResources
    );
    if (nonNull(linkedInstance)) {
      var edge = new ResourceEdge(linkedInstance, work, INSTANTIATES);
      linkedInstance.addOutgoingEdge(edge);
      work.addIncomingEdge(edge);
    }
    work.setLabel(basicTitle.getLabel());
    return work;
  }

  private static Resource createContent() {
    var categorySet = createResource(
      Map.of(
        LINK, List.of("http://id.loc.gov/vocabulary/genreFormSchemes/rdacontent"),
        LABEL, List.of("rdacontent")
      ),
      Set.of(CATEGORY_SET),
      emptyMap())
      .setLabel("rdacontent");
    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(IS_DEFINED_BY, List.of(categorySet));
    return createResource(
      Map.of(
        TERM, List.of("text"),
        LINK, List.of("http://id.loc.gov/vocabulary/contentTypes/txt"),
        CODE, List.of("txt"),
        SOURCE, List.of("content source")
      ),
      Set.of(CATEGORY),
      pred2OutgoingResources
    ).setLabel("text");
  }

  private static Resource createTargetAudience() {
    var categorySet = createResource(
      Map.of(
        LINK, List.of("http://id.loc.gov/vocabulary/maudience"),
        LABEL, List.of("Target audience")
      ),
      Set.of(CATEGORY_SET),
      emptyMap())
      .setLabel("Target audience");
    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(IS_DEFINED_BY, List.of(categorySet));
    return createResource(
      Map.of(
        TERM, List.of("Primary"),
        LINK, List.of("http://id.loc.gov/vocabulary/maudience/pri"),
        CODE, List.of("b")
      ),
      Set.of(CATEGORY),
      pred2OutgoingResources
    ).setLabel("Primary");
  }

  private Resource status(String prefix) {
    return createResource(
      Map.of(
        LABEL, List.of(prefix + " status value"),
        LINK, List.of(prefix + " status link")
      ),
      Set.of(ResourceTypeDictionary.STATUS),
      emptyMap()
    ).setLabel(prefix + " status value");
  }

  public Resource providerEvent(String type) {
    return createResource(
      Map.of(
        DATE, List.of(type + " date"),
        NAME, List.of(type + " name"),
        PROVIDER_DATE, List.of(type + " provider date"),
        SIMPLE_PLACE, List.of(type + " simple place")
      ),
      Set.of(PROVIDER_EVENT),
      Map.of(PROVIDER_PLACE, List.of(providerPlace(type)))
    ).setLabel(type + " name");
  }

  private Resource providerPlace(String providerEventType) {
    return createResource(
      Map.of(
        CODE, List.of(providerEventType + " providerPlace code"),
        LABEL, List.of(providerEventType + " providerPlace label"),
        LINK, List.of(providerEventType + " providerPlace link")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel(providerEventType + " providerPlace label");
  }

  public static Resource createResource(Map<PropertyDictionary, List<String>> propertiesDic,
                                        Set<ResourceTypeDictionary> types,
                                        Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    var resource = new Resource();
    pred2OutgoingResources.keySet()
      .stream()
      .flatMap(pred -> pred2OutgoingResources.get(pred)
        .stream()
        .map(target -> new ResourceEdge(resource, target, pred)))
      .forEach(resource::addOutgoingEdge);

    var properties = propertiesDic.entrySet().stream().collect(toMap(e -> e.getKey().getValue(), Map.Entry::getValue));
    resource.setDoc(getJsonNode(properties));
    types.forEach(resource::addTypes);
    resource.setId(randomLong());
    return resource;
  }

}
