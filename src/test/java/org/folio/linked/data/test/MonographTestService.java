package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.util.Bibframe2Constants.AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO;
import static org.folio.linked.data.util.Bibframe2Constants.APPLIES_TO_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CARRIER2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.CARRIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION;
import static org.folio.linked.data.util.Bibframe2Constants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.COPYRIGHT_DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DIMENSIONS_URL;
import static org.folio.linked.data.util.Bibframe2Constants.DISTRIBUTION;
import static org.folio.linked.data.util.Bibframe2Constants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ELECTRONIC_LOCATOR_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT;
import static org.folio.linked.data.util.Bibframe2Constants.EXTENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.FAMILY;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.Bibframe2Constants.IMM_ACQUISITION;
import static org.folio.linked.data.util.Bibframe2Constants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_2;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ISSUANCE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ISSUANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.JURISDICTION;
import static org.folio.linked.data.util.Bibframe2Constants.LABEL_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MANUFACTURE;
import static org.folio.linked.data.util.Bibframe2Constants.MEDIA2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.MEDIA_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MEETING;
import static org.folio.linked.data.util.Bibframe2Constants.MONOGRAPH_2;
import static org.folio.linked.data.util.Bibframe2Constants.NON_SORT_NUM_URL;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_2;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_TYPE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_TYPE_URI;
import static org.folio.linked.data.util.Bibframe2Constants.ORGANIZATION;
import static org.folio.linked.data.util.Bibframe2Constants.PARALLEL_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NAME_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NUMBER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PERSON;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE2;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PLACE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PRODUCTION;
import static org.folio.linked.data.util.Bibframe2Constants.PROJECTED_PROVISION_DATE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_ID;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_LABEL;
import static org.folio.linked.data.util.Bibframe2Constants.PROPERTY_URI;
import static org.folio.linked.data.util.Bibframe2Constants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION;
import static org.folio.linked.data.util.Bibframe2Constants.QUALIFIER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.RESPONSIBILITY_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.ROLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SAME_AS_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.STATUS2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SUBTITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SUPP_CONTENT;
import static org.folio.linked.data.util.Bibframe2Constants.SUPP_CONTENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.URL_URL;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.VARIANT_TYPE_URL;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNING_SOURCE;
import static org.folio.linked.data.util.BibframeConstants.CARRIER;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_DATE;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE;
import static org.folio.linked.data.util.BibframeConstants.LABEL;
import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.TERM;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MonographTestService {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final CoreMapper coreMapper;

  public ResourceType getMonographType() {
    return resourceTypeService.get(MONOGRAPH);
  }

  public ResourceType getMonograph2Type() {
    return resourceTypeService.get(MONOGRAPH_2);
  }

  public Resource createSampleMonograph() {
    var instance = createSampleInstance();
    return createResource(
      emptyMap(),
      MONOGRAPH,
      Map.of(INSTANCE, List.of(instance))
    );
  }

  public Resource createSampleMonograph_2() {
    var instance = createSampleInstance2();
    return createResource(
      emptyMap(),
      MONOGRAPH_2,
      Map.of(INSTANCE_URL, List.of(instance))
    );
  }

  private Resource createSampleInstance() {
    var instanceTitle = createResource(
      Map.of(
        PART_NAME, List.of("Instance: partName"),
        PART_NUMBER, List.of("Instance: partNumber"),
        MAIN_TITLE, List.of("Instance: Laramie holds the range"),
        NON_SORT_NUM, List.of("Instance: nonSortNum"),
        SUBTITLE, List.of("Instance: subtitle")
      ),
      INSTANCE_TITLE,
      emptyMap()
    );
    var parallelTitle = createResource(
      Map.of(
        PART_NAME, List.of("Parallel: partName"),
        PART_NUMBER, List.of("Parallel: partNumber"),
        MAIN_TITLE, List.of("Parallel: Laramie holds the range"),
        DATE, List.of("Parallel: date"),
        SUBTITLE, List.of("Parallel: subtitle"),
        NOTE, List.of("Parallel: noteLabel")
      ),
      PARALLEL_TITLE,
      emptyMap()
    );
    var variantTitle = createResource(
      Map.of(
        PART_NAME, List.of("Variant: partName"),
        PART_NUMBER, List.of("Variant: partNumber"),
        MAIN_TITLE, List.of("Variant: Laramie holds the range"),
        DATE, List.of("Variant: date"),
        SUBTITLE, List.of("Variant: subtitle"),
        VARIANT_TYPE, List.of("Variant: variantType"),
        NOTE, List.of("Variant: noteLabel")
      ),
      VARIANT_TITLE,
      emptyMap()
    );

    var production = providerEvent("production");
    var publication = providerEvent("publication");
    var distribution = providerEvent("distribution");
    var manufacture = providerEvent("manufacture");

    var accessLocation = createResource(
      Map.of(
        LINK, List.of("accessLocationValue"),
        NOTE, List.of("accessLocationNote")
      ),
      ACCESS_LOCATION,
      emptyMap()
    );

    var lccn = createResource(
      Map.of(NAME, List.of("lccn value")),
      LCCN,
      Map.of(STATUS_PRED, List.of(status("lccn")))
    );

    var isbn = createResource(
      Map.of(
        NAME, List.of("isbn value"),
        QUALIFIER, List.of("isbn qualifier")
      ),
      ISBN,
      Map.of(STATUS_PRED, List.of(status("isbn")))
    );

    var ean = createResource(
      Map.of(
        EAN_VALUE, List.of("ean value"),
        QUALIFIER, List.of("ean qualifier")
      ),
      EAN,
      emptyMap()
    );

    var localId = createResource(
      Map.of(
        LOCAL_ID_VALUE, List.of("localId value"),
        ASSIGNING_SOURCE, List.of("localId assigner")
      ),
      LOCAL_ID,
      emptyMap()
    );

    var otherId = createResource(
      Map.of(
        NAME, List.of("otherId value"),
        QUALIFIER, List.of("otherId qualifier")
      ),
      OTHER_ID,
      emptyMap()
    );

    var media = createResource(
      Map.of(
        CODE, List.of("media code"),
        TERM, List.of("unmediated"),
        LINK, List.of("media link")
      ),
      MEDIA,
      emptyMap()
    );

    var carrier = createResource(
      Map.of(
        CODE, List.of("carrier code"),
        TERM, List.of("carrier 1"),
        LINK, List.of("carrier link")
      ),
      CARRIER,
      emptyMap()
    );

    var pred2OutgoingResources = new LinkedHashMap<String, List<Resource>>();
    pred2OutgoingResources.put(INSTANCE_TITLE_PRED, List.of(instanceTitle, parallelTitle, variantTitle));
    pred2OutgoingResources.put(PRODUCTION_PRED, List.of(production));
    pred2OutgoingResources.put(PUBLICATION_PRED, List.of(publication));
    pred2OutgoingResources.put(DISTRIBUTION_PRED, List.of(distribution));
    pred2OutgoingResources.put(MANUFACTURE_PRED, List.of(manufacture));
    pred2OutgoingResources.put(ACCESS_LOCATION_PRED, List.of(accessLocation));
    pred2OutgoingResources.put(MAP_PRED, List.of(lccn, isbn, ean, localId, otherId));
    pred2OutgoingResources.put(MEDIA_PRED, List.of(media));
    pred2OutgoingResources.put(CARRIER_PRED, List.of(carrier));

    return createResource(
      Map.of(
        DIMENSIONS, List.of("20 cm"),
        RESPONSIBILITY_STATEMENT, List.of("responsibility statement"),
        EDITION_STATEMENT, List.of("edition statement"),
        COPYRIGHT_DATE, List.of("copyright date"),
        PROJECTED_PROVISION_DATE, List.of("projected provision date"),
        ISSUANCE, List.of("single unit")
      ),
      INSTANCE,
      pred2OutgoingResources
    );
  }

  private Resource status(String prefix) {
    return createResource(
      Map.of(
        LABEL, List.of(prefix + " status label"),
        LINK, List.of(prefix + " status link")
      ),
      STATUS,
      emptyMap()
    );
  }

  private Resource providerEvent(String type) {
    return createResource(
      Map.of(
        DATE, List.of(type + " date"),
        NAME, List.of(type + " name"),
        SIMPLE_DATE, List.of(type + " simple date"),
        SIMPLE_PLACE, List.of(type + " simple place")
      ),
      PROVIDER_EVENT,
      Map.of(PLACE_PRED, List.of(place(type)))
    );
  }

  private Resource place(String providerEventType) {
    return createResource(
      Map.of(
        NAME, List.of(providerEventType + " place name"),
        LINK, List.of(providerEventType + " place link")
      ),
      PLACE,
      emptyMap()
    );
  }

  private Resource createSampleInstance2() {
    var instanceTitle = createResource(
      Map.of(
        PART_NAME_URL, List.of("Instance: partName"),
        PART_NUMBER_URL, List.of("Instance: partNumber"),
        MAIN_TITLE_PRED, List.of("Instance: Laramie holds the range"),
        NON_SORT_NUM_URL, List.of("Instance: nonSortNum"),
        SUBTITLE_URL, List.of("Instance: subtitle")
      ),
      INSTANCE_TITLE_2,
      emptyMap()
    );
    var parallelTitle = createResource(
      Map.of(
        PART_NAME_URL, List.of("Parallel: partName"),
        PART_NUMBER_URL, List.of("Parallel: partNumber"),
        MAIN_TITLE_PRED, List.of("Parallel: Laramie holds the range"),
        DATE_URL, List.of("Parallel: date"),
        SUBTITLE_URL, List.of("Parallel: subtitle")
      ),
      PARALLEL_TITLE_2,
      Map.of(NOTE_PRED, List.of(getSimpleNote("Parallel: noteLabel")))
    );
    var variantTitle = createResource(
      Map.of(
        PART_NAME_URL, List.of("Variant: partName"),
        PART_NUMBER_URL, List.of("Variant: partNumber"),
        MAIN_TITLE_PRED, List.of("Variant: Laramie holds the range"),
        DATE_URL, List.of("Variant: date"),
        SUBTITLE_URL, List.of("Variant: subtitle"),
        VARIANT_TYPE_URL, List.of("Variant: variantType")
      ),
      VARIANT_TITLE_2,
      Map.of(NOTE_PRED, List.of(getSimpleNote("Variant: noteLabel")))
    );

    var distribution = provisionActivity("Distribution: ", DISTRIBUTION);
    var manufacture = provisionActivity("Manufacture: ", MANUFACTURE);
    var production = provisionActivity("Production: ", PRODUCTION);
    var publication = provisionActivity("Publication: ", PUBLICATION);

    var agentPerson = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "Spearman, Frank H. (Frank Hamilton), 1859-1937",
          PROPERTY_URI, "http://id.loc.gov/authorities/names/n87914389")
        )
      ), PERSON,
      emptyMap());

    var roleAuthor = createPropertyResource(
      ROLE,
      "Author",
      ROLE_URL,
      ROLE_URL
    );

    var contribPerson = createResource(
      emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(agentPerson),
        ROLE_PRED, List.of(roleAuthor)
      )
    );

    var agentFamily = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "Hopwood family",
          PROPERTY_URI, "http://id.loc.gov/authorities/subjects/sh85061960")
        )
      ), FAMILY,
      emptyMap());

    var roleContributor = createPropertyResource(
      ROLE,
      "Contributor",
      ROLE_URL,
      ROLE_URL
    );

    var contribFamily = createResource(
      emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(agentFamily),
        ROLE_PRED, List.of(roleContributor)
      )
    );

    var agentOrganization = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "Charles Scribner's Sons",
          PROPERTY_URI, "http://id.loc.gov/authorities/names/n81050810")
        )
      ), ORGANIZATION,
      emptyMap());

    var roleProvider = createPropertyResource(
      ROLE,
      "Provider",
      ROLE_URL,
      ROLE_URL
    );

    var contribOrganization = createResource(
      emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(agentOrganization),
        ROLE_PRED, List.of(roleProvider)
      )
    );

    var agentJurisdiction = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "United States. Congress. House. Library",
          PROPERTY_URI, "http://id.loc.gov/authorities/names/n87837615")
        )
      ), JURISDICTION,
      emptyMap());

    var roleContrator = createPropertyResource(
      ROLE,
      "Contractor",
      ROLE_URL,
      ROLE_URL
    );

    var contibJurisdiction = createResource(
      emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(agentJurisdiction),
        ROLE_PRED, List.of(roleContrator)
      )
    );

    var agentMeeting = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "Workshop on Electronic Texts (1992 : Library of Congress)",
          PROPERTY_URI, "http://id.loc.gov/authorities/names/nr93009771")
        )
      ), MEETING,
      emptyMap());

    var roleOther = createPropertyResource(
      ROLE,
      "Other",
      ROLE_URL,
      ROLE_URL
    );

    var contibMeeting = createResource(
      emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(agentMeeting),
        ROLE_PRED, List.of(roleOther)
      )
    );

    var ean = createResource(
      Map.of(
        VALUE_PRED, List.of("12345670"),
        QUALIFIER_URL, List.of("07654321")
      ),
      IDENTIFIERS_EAN,
      emptyMap()
    );
    var isbn = createResource(
      Map.of(
        VALUE_PRED, List.of("12345671"),
        QUALIFIER_URL, List.of("17654321"),
        STATUS2_PRED, List.of(Map.of(
          PROPERTY_ID, "isbnStatusId",
          PROPERTY_LABEL, "isbnStatusLabel",
          PROPERTY_URI, "isbnStatusUri")
        )
      ),
      IDENTIFIERS_ISBN,
      emptyMap()
    );
    var lccn = createResource(
      Map.of(
        VALUE_PRED, List.of("12345672"),
        STATUS2_PRED, List.of(Map.of(
          PROPERTY_ID, "lccnStatusId",
          PROPERTY_LABEL, "lccnStatusLabel",
          PROPERTY_URI, "lccnStatusUri")
        )
      ),
      IDENTIFIERS_LCCN,
      emptyMap()
    );
    var local = createResource(
      Map.of(
        VALUE_PRED, List.of("12345673"),
        ASSIGNER_PRED, List.of(Map.of(
          PROPERTY_ID, "assignerId",
          PROPERTY_LABEL, "assignerLabel",
          PROPERTY_URI, "assignerUri")
        )
      ),
      IDENTIFIERS_LOCAL,
      emptyMap()
    );
    var other = createResource(
      Map.of(
        VALUE_PRED, List.of("12345674"),
        QUALIFIER_URL, List.of("47654321")
      ),
      IDENTIFIERS_OTHER,
      emptyMap()
    );

    var note = createResource(
      Map.of(LABEL_PRED, List.of("note label")),
      NOTE_2,
      Map.of(NOTE_TYPE_PRED, List.of(createPropertyResource(
        "noteTypeId",
        "Accompanying material",
        "http://id.loc.gov/vocabulary/mnotetype/accmat",
        NOTE_TYPE_URI
      )))
    );

    var appliesTo = createResource(
      Map.of(LABEL_PRED, List.of("extent appliesTo label")),
      APPLIES_TO,
      emptyMap()
    );

    var extent = createResource(
      Map.of(LABEL_PRED, List.of("extent label")),
      EXTENT,
      Map.of(
        NOTE_PRED, List.of(getSimpleNote("extent note label")),
        APPLIES_TO_PRED, List.of(appliesTo)
      )
    );

    var issuance = createPropertyResource(
      "issuanceId",
      "single unit",
      ISSUANCE_URL,
      ISSUANCE_URL
    );

    var carrier = createPropertyResource(
      "carrierId",
      "volume",
      CARRIER_URL,
      CARRIER_URL
    );

    var media = createPropertyResource(
      "mediaId",
      "unmediated",
      MEDIA_URL,
      MEDIA_URL
    );

    var supplementaryContent = createResource(
      Map.of(
        LABEL_PRED, List.of("supplementaryContentLabel"),
        VALUE_PRED, List.of("supplementaryContentValue")
      ),
      SUPP_CONTENT,
      emptyMap()
    );

    var immediateAcquisition = createResource(
      Map.of(
        APPLICABLE_INSTITUTION_PRED, List.of(Map.of(
          PROPERTY_ID, "applicableInstitutionId",
          PROPERTY_LABEL, "some applicableInstitution",
          PROPERTY_URI, APPLICABLE_INSTITUTION_URL
        )),
        LABEL_PRED, List.of("some immediateAcquisition")
      ),
      IMM_ACQUISITION,
      emptyMap()
    );

    var electronicLocator = createResource(
      Map.of(VALUE_PRED, List.of("electronicLocatorValue")),
      URL_URL,
      Map.of(NOTE_PRED, List.of(getSimpleNote("electronicLocatorNoteLabel")))
    );

    var pred2OutgoingResources = new LinkedHashMap<String, List<Resource>>();
    pred2OutgoingResources.put(INSTANCE_TITLE_2_PRED, List.of(instanceTitle, parallelTitle, variantTitle));
    pred2OutgoingResources.put(PROVISION_ACTIVITY_PRED, List.of(distribution, manufacture, production, publication));
    pred2OutgoingResources.put(CONTRIBUTION_PRED, List.of(contribPerson, contribFamily, contribOrganization,
      contibJurisdiction, contibMeeting));
    pred2OutgoingResources.put(IDENTIFIED_BY_PRED, List.of(ean, isbn, lccn, local, other));
    pred2OutgoingResources.put(NOTE_PRED, List.of(note));
    pred2OutgoingResources.put(EXTENT_PRED, List.of(extent));
    pred2OutgoingResources.put(ISSUANCE_PRED, List.of(issuance));
    pred2OutgoingResources.put(CARRIER2_PRED, List.of(carrier));
    pred2OutgoingResources.put(MEDIA2_PRED, List.of(media));
    pred2OutgoingResources.put(IMM_ACQUISITION_PRED, List.of(immediateAcquisition));
    pred2OutgoingResources.put(SUPP_CONTENT_PRED, List.of(supplementaryContent));
    pred2OutgoingResources.put(ELECTRONIC_LOCATOR_2_PRED, List.of(electronicLocator));

    return createResource(
      Map.of(
        DIMENSIONS_URL, List.of("20 cm"),
        RESPONSIBILITY_STATEMENT_URL, List.of("responsibility statement"),
        EDITION_STATEMENT_URL, List.of("edition statement"),
        COPYRIGHT_DATE_URL, List.of("copyright date"),
        PROJECTED_PROVISION_DATE_URL, List.of("projected provision date")
      ),
      INSTANCE_2,
      pred2OutgoingResources
    );
  }

  @NotNull
  private Resource getSimpleNote(String label) {
    return createResource(
      Map.of(LABEL_PRED, List.of(label)),
      NOTE_2,
      emptyMap());
  }

  private Resource provisionActivity(String prefix, String url) {
    return createResource(
      Map.of(
        DATE_PRED, List.of(prefix + "1921"),
        SIMPLE_DATE_PRED, List.of(prefix + "1921"),
        SIMPLE_AGENT_PRED, List.of(prefix + "Charles Scribner's Sons"),
        SIMPLE_PLACE_PRED, List.of(prefix + "New York")
      ),
      url,
      Map.of(PLACE2_PRED, List.of(place2(prefix)))
    );
  }

  private Resource place2(String prefix) {
    return createPropertyResource(
      PLACE2,
      prefix + "New York (State)",
      PLACE_URL,
      PLACE_URL
    );
  }

  private Resource createResource(Map<String, List<?>> properties, String typeLabel,
                                  Map<String, List<Resource>> pred2OutgoingResources) {
    var resource = new Resource();
    pred2OutgoingResources.keySet()
      .stream()
      .map(predicateService::get)
      .flatMap(pred -> pred2OutgoingResources.get(pred.getLabel())
        .stream()
        .map(target -> new ResourceEdge(resource, target, pred)))
      .forEach(edge -> resource.getOutgoingEdges().add(edge));

    resource.setDoc(getJsonNode(properties));
    resource.addType(resourceTypeService.get(typeLabel));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private Resource createPropertyResource(String id, String label, String uri, String typeUri) {
    var resource = new Resource();
    resource.addType(resourceTypeService.get(typeUri));
    var doc = getJsonNode(new HashMap<>(Map.of(
      PROPERTY_ID, id,
      PROPERTY_URI, uri,
      PROPERTY_LABEL, label)
    ));
    resource.setDoc(doc);
    resource.setLabel(label);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

}
