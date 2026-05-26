package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.EXTENT;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.IS_DEFINED_BY;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
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
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_END;
import static org.folio.ld.dictionary.PropertyDictionary.DATE_START;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_AREA_CODE;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.ITEM_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
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
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY_SET;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;

@UtilityClass
public class MonographTestUtil {

  public static final long JURISDICTION_CREATOR_ID = 1000000000000000001L;
  public static final String JURISDICTION_CREATOR_LABEL = "jurisdiction creator";
  public static final long JURISDICTION_CONTRIBUTOR_ID = 1000000000000000002L;
  public static final String JURISDICTION_CONTRIBUTOR_LABEL = "jurisdiction contributor";

  public static Resource getSampleInstanceResource() {
    return getSampleInstanceResource(null, getSampleWork());
  }

  public static Resource getSampleInstanceResource(Long id) {
    return getSampleInstanceResource(id, getSampleWork());
  }

  public static Resource getSampleInstanceResource(Long id, Resource linkedWork) {
    var primaryTitle = createPrimaryTitle(id);

    var production = providerEvent("production", "af", "Afghanistan");
    var publication = providerEvent("publication", "al", "Albania");
    var distribution = providerEvent("distribution", "dz", "Algeria");
    var manufacture = providerEvent("manufacture", "as", "American Samoa");

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

    var ian = createResource(
      Map.of(
        NAME, List.of("ian value"),
        QUALIFIER, List.of("ian qualifier")
      ),
      Set.of(IDENTIFIER, ID_IAN),
      emptyMap()
    ).setLabel("ian value");

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

    var extent = createResource(
      Map.of(
        LABEL, List.of("extent label"),
        MATERIALS_SPECIFIED, List.of("materials spec"),
        NOTE, List.of("extent note")
      ),
      Set.of(ResourceTypeDictionary.EXTENT),
      emptyMap()
    ).setLabel("extent label");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(primaryTitle, createParallelTitle(), createVariantTitle()));
    pred2OutgoingResources.put(PE_PRODUCTION, List.of(production));
    pred2OutgoingResources.put(PE_PUBLICATION, List.of(publication));
    pred2OutgoingResources.put(PE_DISTRIBUTION, List.of(distribution));
    pred2OutgoingResources.put(PE_MANUFACTURE, List.of(manufacture));
    pred2OutgoingResources.put(PredicateDictionary.SUPPLEMENTARY_CONTENT, List.of(supplementaryContent));
    pred2OutgoingResources.put(ACCESS_LOCATION, List.of(accessLocation));
    pred2OutgoingResources.put(MAP, List.of(lccn, isbn, ian, otherId));
    pred2OutgoingResources.put(MEDIA, List.of(media));
    pred2OutgoingResources.put(CARRIER, List.of(carrier));
    pred2OutgoingResources.put(COPYRIGHT, List.of(copyrightEvent));
    pred2OutgoingResources.put(EXTENT, List.of(extent));

    var instance = createResource(
      Map.ofEntries(
        entry(DIMENSIONS, List.of("20 cm")),
        entry(EDITION, List.of("edition statement")),
        entry(PROJECTED_PROVISION_DATE, List.of("projected provision date")),
        entry(ISSUANCE, List.of("single unit")),
        entry(STATEMENT_OF_RESPONSIBILITY, List.of("statement of responsibility"))
      ),
      Set.of(INSTANCE),
      pred2OutgoingResources
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1")
        .setSrsId("43d58061-decf-4d74-9747-0e1c368e861b")
    );
    if (nonNull(id)) {
      instance.setIdAndRefreshEdges(id);
    }
    instance.setLabel(primaryTitle.getLabel());
    if (nonNull(linkedWork)) {
      var edge = new ResourceEdge(instance, linkedWork, INSTANTIATES);
      instance.addOutgoingEdge(edge);
      linkedWork.addIncomingEdge(edge);
    }
    return instance;
  }

  public static Resource createPrimaryTitle(Long id) {
    var primaryTitleValue = "Primary: mainTitle" + (nonNull(id) ? id : "");
    var subTitleValue = "Primary: subTitle";
    var partNumberValue = "Primary: partNumber";
    var partNameValue = "Primary: partName";

    return createResource(
      Map.of(
        PART_NAME, List.of(partNameValue),
        PART_NUMBER, List.of(partNumberValue),
        MAIN_TITLE, List.of(primaryTitleValue),
        NON_SORT_NUM, List.of("Primary: nonSortNum"),
        SUBTITLE, List.of(subTitleValue)
      ),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel(primaryTitleValue + " " + subTitleValue + " " + partNumberValue + " " + partNameValue);
  }

  private static Resource createParallelTitle() {
    var mainTitle = "Parallel: mainTitle";
    var subTitle = "Parallel: subTitle";
    return createResource(
      Map.of(
        PART_NAME, List.of("Parallel: partName"),
        PART_NUMBER, List.of("Parallel: partNumber"),
        MAIN_TITLE, List.of(mainTitle),
        DATE, List.of("Parallel: date"),
        SUBTITLE, List.of(subTitle),
        NOTE, List.of("Parallel: noteLabel")
      ),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel(mainTitle + " " + subTitle);
  }

  private static Resource createVariantTitle() {
    var mainTitle = "Variant: mainTitle";
    var subTitle = "Variant: subTitle";
    return createResource(
      Map.of(
        PART_NAME, List.of("Variant: partName"),
        PART_NUMBER, List.of("Variant: partNumber"),
        MAIN_TITLE, List.of(mainTitle),
        DATE, List.of("Variant: date"),
        SUBTITLE, List.of(subTitle),
        VARIANT_TYPE, List.of("Variant: variantType"),
        NOTE, List.of("Variant: noteLabel")
      ),
      Set.of(VARIANT_TITLE),
      emptyMap()
    ).setLabel(mainTitle + " " + subTitle);
  }

  private static Resource createVariantTitleWithType(String mainTitle, String type) {
    return createResource(
      Map.of(
        MAIN_TITLE, List.of(mainTitle),
        VARIANT_TYPE, List.of(type)
      ),
      Set.of(VARIANT_TITLE),
      emptyMap()
    ).setLabel(mainTitle);
  }

  public static Resource getSampleInstanceWithWorkTitlesForRdfExport() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Instance: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Instance: mainTitle");
    var instanceParallelTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Instance Parallel: mainTitle")),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel("Instance Parallel: mainTitle");
    var instanceVariantTitle = createVariantTitleWithType("Instance Variant: mainTitle", "0");

    var instancePred2Outgoing = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    instancePred2Outgoing.put(TITLE, List.of(instancePrimaryTitle, instanceParallelTitle, instanceVariantTitle));

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      instancePred2Outgoing
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("4387gb6d-223f-68d8-cg4f-73defg369743")
        .setSrsId("65f7a283-feh1-6f39-9969-2g3e58ag083d")
    );
    instance.setLabel("Instance: mainTitle");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Work: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Work: mainTitle");
    var workParallelTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Work Parallel: mainTitle")),
      Set.of(PARALLEL_TITLE),
      emptyMap()
    ).setLabel("Work Parallel: mainTitle");
    var workVariantTitle = createVariantTitleWithType("Work Variant: mainTitle", "0");
    var workPred2Outgoing = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    workPred2Outgoing.put(TITLE, List.of(workPrimaryTitle, workParallelTitle, workVariantTitle));
    var work = createResource(
      Map.of(LINK, List.of(UUID.randomUUID().toString())),
      Set.of(WORK, BOOKS),
      workPred2Outgoing
    ).setLabel("Work: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceResourceForRdfExport() {
    var primaryTitle = createPrimaryTitle(null);
    var parallelTitle = createParallelTitle();
    var variantTitlePor = createVariantTitleWithType("Variant por: mainTitle", "0");
    var variantTitleDis = createVariantTitleWithType("Variant dis: mainTitle", "2");
    var variantTitleCov = createVariantTitleWithType("Variant cov: mainTitle", "4");
    var variantTitleAtp = createVariantTitleWithType("Variant atp: mainTitle", "5");
    var variantTitleCap = createVariantTitleWithType("Variant cap: mainTitle", "6");
    var variantTitleRun = createVariantTitleWithType("Variant run: mainTitle", "7");
    var variantTitleSpi = createVariantTitleWithType("Variant spi: mainTitle", "8");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(
      primaryTitle, parallelTitle,
      variantTitlePor, variantTitleDis, variantTitleCov, variantTitleAtp,
      variantTitleCap, variantTitleRun, variantTitleSpi
    ));

    var instance = createResource(
      Map.ofEntries(
        entry(DIMENSIONS, List.of("20 cm")),
        entry(STATEMENT_OF_RESPONSIBILITY, List.of("statement of responsibility"))
      ),
      Set.of(INSTANCE),
      pred2OutgoingResources
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("3276fa5c-112e-57c7-bf3e-62cdef258632")
        .setSrsId("54e69172-edg0-5e28-8858-1f2d479f972c")
    );
    instance.setLabel(primaryTitle.getLabel());

    var linkedWork = getSampleWork();
    var edge = new ResourceEdge(instance, linkedWork, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    linkedWork.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithWorkCreatorLccn() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Creator LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Creator LCCN: mainTitle");

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      Map.of(TITLE, List.of(instancePrimaryTitle))
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
        .setSrsId("7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d")
    );
    instance.setLabel("Creator LCCN: mainTitle");

    var lcnafIdentifier = createResource(
      Map.of(
        NAME, List.of("n2021004098"),
        LINK, List.of("http://id.loc.gov/authorities/n2021004098")
      ),
      Set.of(IDENTIFIER, ID_LCNAF),
      Map.of(STATUS, List.of(createResource(
        Map.of(
          LABEL, List.of("current"),
          LINK, List.of("http://id.loc.gov/vocabulary/mstatus/current")
        ),
        Set.of(ResourceTypeDictionary.STATUS),
        emptyMap()
      )))
    ).setLabel("n2021004098");

    var creator = createResource(
      Map.of(NAME, List.of("n2021004098")),
      Set.of(PERSON),
      Map.of(MAP, List.of(lcnafIdentifier))
    ).setLabel("n2021004098");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Creator LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Creator LCCN: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      new LinkedHashMap<>(Map.of(
        TITLE, List.of(workPrimaryTitle),
        CREATOR, List.of(creator)
      ))
    ).setLabel("Creator LCCN: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithWorkCreatorNoLccn() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Creator No LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Creator No LCCN: mainTitle");

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      Map.of(TITLE, List.of(instancePrimaryTitle))
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e")
        .setSrsId("8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e")
    );
    instance.setLabel("Creator No LCCN: mainTitle");

    var creator = createResource(
      Map.of(NAME, List.of("Creator No LCCN")),
      Set.of(PERSON),
      emptyMap()
    ).setLabel("Creator No LCCN");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Creator No LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Creator No LCCN: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      new LinkedHashMap<>(Map.of(
        TITLE, List.of(workPrimaryTitle),
        CREATOR, List.of(creator)
      ))
    ).setLabel("Creator No LCCN: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithWorkSubjectLccn() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Subject LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Subject LCCN: mainTitle");

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      Map.of(TITLE, List.of(instancePrimaryTitle))
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f")
        .setSrsId("9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f")
    );
    instance.setLabel("Subject LCCN: mainTitle");

    var lcnafIdentifier = createResource(
      Map.of(
        NAME, List.of("n2021009876"),
        LINK, List.of("http://id.loc.gov/authorities/n2021009876")
      ),
      Set.of(IDENTIFIER, ID_LCNAF),
      Map.of(STATUS, List.of(createResource(
        Map.of(
          LABEL, List.of("current"),
          LINK, List.of("http://id.loc.gov/vocabulary/mstatus/current")
        ),
        Set.of(ResourceTypeDictionary.STATUS),
        emptyMap()
      )))
    ).setLabel("n2021009876");

    var subjectPerson = createResource(
      Map.of(LABEL, List.of("Subject Person LCCN"), NAME, List.of("Subject Person LCCN")),
      Set.of(PERSON),
      Map.of(MAP, List.of(lcnafIdentifier))
    ).setLabel("Subject Person LCCN");

    var subjectConcept = createResource(
      Map.of(LABEL, List.of("Subject Person LCCN")),
      Set.of(CONCEPT, PERSON),
      Map.of(FOCUS, List.of(subjectPerson))
    ).setLabel("Subject Person LCCN");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Subject LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Subject LCCN: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      new LinkedHashMap<>(Map.of(
        TITLE, List.of(workPrimaryTitle),
        SUBJECT, List.of(subjectConcept)
      ))
    ).setLabel("Subject LCCN: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithWorkSubjectNoLccn() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Subject No LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Subject No LCCN: mainTitle");

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      Map.of(TITLE, List.of(instancePrimaryTitle))
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a")
        .setSrsId("0d1e2f3a-4b5c-6d7e-8f9a-0b1c2d3e4f5a")
    );
    instance.setLabel("Subject No LCCN: mainTitle");

    var subjectPerson = createResource(
      Map.of(LABEL, List.of("Subject No LCCN Person"), NAME, List.of("Subject No LCCN Person")),
      Set.of(PERSON),
      emptyMap()
    ).setLabel("Subject No LCCN Person");

    var subjectConcept = createResource(
      Map.of(LABEL, List.of("Subject No LCCN Person")),
      Set.of(CONCEPT, PERSON),
      Map.of(FOCUS, List.of(subjectPerson))
    ).setLabel("Subject No LCCN Person");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Subject No LCCN: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Subject No LCCN: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      new LinkedHashMap<>(Map.of(
        TITLE, List.of(workPrimaryTitle),
        SUBJECT, List.of(subjectConcept)
      ))
    ).setLabel("Subject No LCCN: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithWorkComplexSubject() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Complex Subject: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Complex Subject: mainTitle");

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      Map.of(TITLE, List.of(instancePrimaryTitle))
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b")
        .setSrsId("1e2f3a4b-5c6d-7e8f-9a0b-1c2d3e4f5a6b")
    );
    instance.setLabel("Complex Subject: mainTitle");

    var focusPerson = createResource(
      Map.of(LABEL, List.of("Complex Subject Person"), NAME, List.of("Complex Subject Person")),
      Set.of(PERSON),
      emptyMap()
    ).setLabel("Complex Subject Person");

    var subFocusTopic = createResource(
      Map.of(LABEL, List.of("Complex Subject Topic"), NAME, List.of("Complex Subject Topic")),
      Set.of(TOPIC),
      emptyMap()
    ).setLabel("Complex Subject Topic");

    var subjectConcept = createResource(
      Map.of(LABEL, List.of("Complex Subject Person -- Complex Subject Topic")),
      Set.of(CONCEPT),
      new LinkedHashMap<>(Map.of(
        FOCUS, List.of(focusPerson),
        SUB_FOCUS, List.of(subFocusTopic)
      ))
    ).setLabel("Complex Subject Person -- Complex Subject Topic");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Complex Subject: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Complex Subject: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      new LinkedHashMap<>(Map.of(
        TITLE, List.of(workPrimaryTitle),
        SUBJECT, List.of(subjectConcept)
      ))
    ).setLabel("Complex Subject: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithProvisionActivities() {
    var instancePrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Provision Activities: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Provision Activities: mainTitle");

    var publication = providerEvent("publication", "al", "Albania");
    var distribution = providerEvent("distribution", "dz", "Algeria");
    var manufacture = providerEvent("manufacture", "as", "American Samoa");
    var production = providerEvent("production", "af", "Afghanistan");

    var instanceEdges = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    instanceEdges.put(TITLE, List.of(instancePrimaryTitle));
    instanceEdges.put(PE_PUBLICATION, List.of(publication));
    instanceEdges.put(PE_DISTRIBUTION, List.of(distribution));
    instanceEdges.put(PE_MANUFACTURE, List.of(manufacture));
    instanceEdges.put(PE_PRODUCTION, List.of(production));

    var instance = createResource(
      emptyMap(),
      Set.of(INSTANCE),
      instanceEdges
    );
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId("6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c")
        .setSrsId("2f3a4b5c-6d7e-8f9a-0b1c-2d3e4f5a6b7c")
    );
    instance.setLabel("Provision Activities: mainTitle");

    var workPrimaryTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Provision Activities: mainTitle")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Provision Activities: mainTitle");

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      Map.of(TITLE, List.of(workPrimaryTitle))
    ).setLabel("Provision Activities: mainTitle");

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    return instance;
  }

  public static Resource getSampleInstanceWithLccnForRdfExport() {
    var lccn = createResource(
      Map.of(NAME, List.of("2010470075")),
      Set.of(IDENTIFIER, ID_LCCN),
      Map.of(STATUS, List.of(createMstatusResource(true)))
    ).setLabel("2010470075");

    var instanceTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Lccn RdfExport")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Lccn RdfExport");

    var instanceEdges = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    instanceEdges.put(TITLE, List.of(instanceTitle));
    instanceEdges.put(MAP, List.of(lccn));

    var instance = createResource(emptyMap(), Set.of(INSTANCE), instanceEdges);
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId(UUID.randomUUID().toString())
        .setSrsId(UUID.randomUUID().toString())
    );
    instance.setLabel("Lccn RdfExport");

    linkNewWork(instance, "Lccn RdfExport");
    return instance;
  }

  public static Resource getSampleInstanceWithIsbnForRdfExport(String isbn, String qualifier, boolean isCurrent) {
    var isbnResource = createResource(
      Map.of(NAME, List.of(isbn), QUALIFIER, List.of(qualifier)),
      Set.of(IDENTIFIER, ID_ISBN),
      Map.of(STATUS, List.of(createMstatusResource(isCurrent)))
    ).setLabel(isbn);

    var instanceTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Isbn RdfExport")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Isbn RdfExport");

    var instanceEdges = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    instanceEdges.put(TITLE, List.of(instanceTitle));
    instanceEdges.put(MAP, List.of(isbnResource));

    var instance = createResource(emptyMap(), Set.of(INSTANCE), instanceEdges);
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId(UUID.randomUUID().toString())
        .setSrsId(UUID.randomUUID().toString())
    );
    instance.setLabel("Isbn RdfExport: " + isbn);

    linkNewWork(instance, "Isbn RdfExport");
    return instance;
  }

  public static Resource getSampleInstanceWithEanForRdfExport(String ean, String qualifier) {
    var properties = qualifier == null
      ? Map.of(NAME, List.of(ean))
      : Map.of(NAME, List.of(ean), QUALIFIER, List.of(qualifier));
    var eanResource = createResource(
      properties,
      Set.of(IDENTIFIER, ID_IAN),
      emptyMap()
    ).setLabel(ean);

    var instanceTitle = createResource(
      Map.of(MAIN_TITLE, List.of("Ean RdfExport")),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel("Ean RdfExport");

    var instanceEdges = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    instanceEdges.put(TITLE, List.of(instanceTitle));
    instanceEdges.put(MAP, List.of(eanResource));

    var instance = createResource(emptyMap(), Set.of(INSTANCE), instanceEdges);
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setSource(LINKED_DATA)
        .setInventoryId(UUID.randomUUID().toString())
        .setSrsId(UUID.randomUUID().toString())
    );
    instance.setLabel("Ean RdfExport: " + ean);

    linkNewWork(instance, "Ean RdfExport");
    return instance;
  }

  private static void linkNewWork(Resource instance, String titleStr) {
    var workTitle = createResource(
      Map.of(MAIN_TITLE, List.of(titleStr)),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel(titleStr);

    var work = createResource(
      emptyMap(),
      Set.of(WORK, BOOKS),
      Map.of(TITLE, List.of(workTitle))
    ).setLabel(titleStr);

    var edge = new ResourceEdge(instance, work, INSTANTIATES);
    instance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);
  }

  private static Resource createMstatusResource(boolean isCurrent) {
    var label = isCurrent ? "current" : "cancinv";
    return createResource(
      Map.of(
        LABEL, List.of(label),
        LINK, List.of("http://id.loc.gov/vocabulary/mstatus/" + label)
      ),
      Set.of(ResourceTypeDictionary.STATUS),
      emptyMap()
    ).setLabel(label);
  }

  public static Resource getSampleWork() {
    return getSampleWork(null);
  }

  public static Resource getSampleWork(Resource linkedInstance) {
    var primaryTitle = createPrimaryTitle(null);

    var unitedStates = createResource(
      Map.of(
        NAME, List.of("United States"),
        GEOGRAPHIC_AREA_CODE, List.of("n-us"),
        GEOGRAPHIC_COVERAGE, List.of("http://id.loc.gov/vocabulary/geographicAreas/n-us")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("United States")
      .setIdAndRefreshEdges(7109832602847218134L);

    var europe = createResource(
      Map.of(
        NAME, List.of("Europe"),
        GEOGRAPHIC_AREA_CODE, List.of("e"),
        GEOGRAPHIC_COVERAGE, List.of("http://id.loc.gov/vocabulary/geographicAreas/e")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("Europe")
      .setIdAndRefreshEdges(-4654600487710655316L);

    var genre1 = createResource(
      Map.of(
        NAME, List.of("genre 1")
      ),
      Set.of(FORM),
      emptyMap()
    ).setLabel("genre 1")
      .setIdAndRefreshEdges(-9064822434663187463L);

    var genre2 = createResource(
      Map.of(
        NAME, List.of("genre 2")
      ),
      Set.of(FORM),
      emptyMap()
    ).setLabel("genre 2")
      .setIdAndRefreshEdges(-4816872480602594231L);

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
        CODE, List.of("fr"),
        LINK, List.of("http://id.loc.gov/vocabulary/countries/fr")
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel("France");

    var language = createResource(
      Map.of(
        CODE, List.of("eng"),
        TERM, List.of("English"),
        LINK, List.of("http://id.loc.gov/vocabulary/languages/eng")
      ),
      Set.of(LANGUAGE_CATEGORY),
      emptyMap()
    ).setLabel("eng");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(primaryTitle, createParallelTitle(), createVariantTitle()));
    pred2OutgoingResources.put(CLASSIFICATION, List.of(createLcClassification(), createDeweyClassification()));
    pred2OutgoingResources.put(CONTENT, List.of(createContent()));
    pred2OutgoingResources.put(SUBJECT, List.of(getSubjectPersonPreferred(), getSubjectFormNotPreferred()));
    pred2OutgoingResources.put(PredicateDictionary.GEOGRAPHIC_COVERAGE, List.of(unitedStates, europe));
    pred2OutgoingResources.put(GENRE, List.of(genre1, genre2));
    pred2OutgoingResources.put(GOVERNMENT_PUBLICATION, List.of(governmentPublication));
    pred2OutgoingResources.put(ORIGIN_PLACE, List.of(originPlace));
    pred2OutgoingResources.put(LANGUAGE, List.of(language));
    pred2OutgoingResources.put(ILLUSTRATIONS, List.of(createIllustrations()));
    pred2OutgoingResources.put(PredicateDictionary.SUPPLEMENTARY_CONTENT, List.of(createSupplementaryContent()));
    pred2OutgoingResources.put(CREATOR,
      List.of(createJurisdictionAgent(JURISDICTION_CREATOR_ID, JURISDICTION_CREATOR_LABEL)));
    pred2OutgoingResources.put(CONTRIBUTOR,
      List.of(createJurisdictionAgent(JURISDICTION_CONTRIBUTOR_ID, JURISDICTION_CONTRIBUTOR_LABEL)));

    var work = createResource(
      Map.ofEntries(
        entry(SUMMARY, List.of("summary text")),
        entry(TABLE_OF_CONTENTS, List.of("table of contents")),
        entry(DATE_START, List.of("2024")),
        entry(DATE_END, List.of("2025"))
      ),
      Set.of(WORK, BOOKS),
      pred2OutgoingResources
    );
    if (nonNull(linkedInstance)) {
      var edge = new ResourceEdge(linkedInstance, work, INSTANTIATES);
      linkedInstance.addOutgoingEdge(edge);
      work.addIncomingEdge(edge);
    }
    work.setLabel(primaryTitle.getLabel());
    return work;
  }

  public static Resource getSampleHub() {
    var primaryTitle = createPrimaryTitle(null);

    var hub = createResource(
      Map.ofEntries(
        entry(LINK, List.of(UUID.randomUUID().toString()))
      ),
      Set.of(HUB),
      Map.of()
    );
    hub.setLabel(primaryTitle.getLabel());
    return hub;
  }

  public static Resource getSubjectPersonPreferred() {
    return getSubjectAndConcept("person", -6999093488677112301L, -3951421359442339069L, PERSON);
  }

  public static Resource getSubjectFormNotPreferred() {
    return getSubjectAndConcept("form", -4718450084121784027L, -354125450028352284L, FORM);
  }

  private static Resource getSubjectAndConcept(String name,
                                               Long subjectId,
                                               Long conceptId,
                                               ResourceTypeDictionary type) {
    var subject = createResource(
      Map.of(NAME, List.of("Subject " + name)),
      Set.of(type),
      emptyMap()
    ).setLabel("subject " + name)
      .setIdAndRefreshEdges(subjectId);

    var subjectConcept = createResource(
      Map.of(
        NAME, List.of("Subject " + name)
      ),
      Set.of(CONCEPT, type),
      emptyMap()
    ).setLabel("subject " + name)
      .setIdAndRefreshEdges(conceptId);

    subjectConcept.addOutgoingEdge(new ResourceEdge(subjectConcept, subject, FOCUS));
    return subjectConcept;
  }

  private static Resource createDeweyClassification() {
    var assigningSource = createResource(
      Map.of(
        NAME, List.of("assigning agency")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("assigning agency")
      .setIdAndRefreshEdges(4932783899755316479L);
    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(PredicateDictionary.ASSIGNING_SOURCE, List.of(assigningSource));
    return createResource(
      Map.of(
        CODE, List.of("ddc code"),
        SOURCE, List.of("ddc"),
        ITEM_NUMBER, List.of("ddc item number"),
        EDITION_NUMBER, List.of("edition number"),
        EDITION, List.of("edition")
      ),
      Set.of(ResourceTypeDictionary.CLASSIFICATION),
      pred2OutgoingResources
    );
  }

  private static Resource createLcClassification() {
    var assigningSource = createResource(
      Map.of(
        NAME, List.of("United States, Library of Congress")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("United States, Library of Congress")
      .setIdAndRefreshEdges(8752404686183471966L);
    return createResource(
      Map.of(
        CODE, List.of("lc code"),
        SOURCE, List.of("lc"),
        ITEM_NUMBER, List.of("lc item number")
      ),
      Set.of(ResourceTypeDictionary.CLASSIFICATION),
      Map.of(
        PredicateDictionary.ASSIGNING_SOURCE, List.of(assigningSource),
        STATUS, List.of(status("lc"))
      )
    );
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

  private static Resource createIllustrations() {
    var categorySet = createResource(
      Map.of(
        LINK, List.of("http://id.loc.gov/vocabulary/millus"),
        LABEL, List.of("Illustrative Content")
      ),
      Set.of(CATEGORY_SET),
      emptyMap())
      .setLabel("Illustrative Content");
    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(IS_DEFINED_BY, List.of(categorySet));
    return createResource(
      Map.of(
        CODE, List.of("a"),
        TERM, List.of("Illustrations"),
        LINK, List.of("http://id.loc.gov/vocabulary/millus/ill")
      ),
      Set.of(CATEGORY),
      pred2OutgoingResources
    ).setLabel("Illustrations");
  }

  private static Resource createSupplementaryContent() {
    var categorySet = createResource(
      Map.of(
        LINK, List.of("http://id.loc.gov/vocabulary/msupplcont"),
        LABEL, List.of("Supplementary Content")
      ),
      Set.of(CATEGORY_SET),
      emptyMap())
      .setLabel("Supplementary Content");
    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(IS_DEFINED_BY, List.of(categorySet));
    return createResource(
      Map.of(
        CODE, List.of("code"),
        TERM, List.of("supplementary content term"),
        LINK, List.of("http://id.loc.gov/vocabulary/msupplcont/code")
      ),
      Set.of(CATEGORY),
      pred2OutgoingResources
    ).setLabel("supplementary content term");
  }

  private Resource status(String prefix) {
    return createResource(
      Map.of(
        LABEL, List.of(prefix + " status value"),
        LINK, List.of("http://id/" + prefix)
      ),
      Set.of(ResourceTypeDictionary.STATUS),
      emptyMap()
    ).setLabel(prefix + " status value");
  }

  public Resource providerEvent(String type, String code, String label) {
    return createResource(
      Map.of(
        DATE, List.of(type + " date"),
        NAME, List.of(type + " name"),
        PROVIDER_DATE, List.of(type + " provider date"),
        SIMPLE_PLACE, List.of(type + " simple place")
      ),
      Set.of(PROVIDER_EVENT),
      Map.of(PROVIDER_PLACE, List.of(providerPlace(code, label)))
    ).setLabel(type + " name");
  }

  private Resource providerPlace(String code, String label) {
    return createResource(
      Map.of(
        CODE, List.of(code),
        LABEL, List.of(label),
        LINK, List.of("http://id.loc.gov/vocabulary/countries/" + code)
      ),
      Set.of(PLACE),
      emptyMap()
    ).setLabel(label);
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
    resource.setIdAndRefreshEdges(randomLong());
    return resource;
  }

  public static void setCurrentStatus(LinkedHashMap instance) {
    var map = (ArrayList) instance.get(MAP.getUri());
    var lccn = (LinkedHashMap) ((LinkedHashMap) map.getFirst()).get(ID_LCCN.getUri());
    var status = (ArrayList) lccn.get(STATUS.getUri());
    ((LinkedHashMap) status.getFirst()).put(LINK.getValue(), List.of("http://id.loc.gov/vocabulary/mstatus/current"));
  }

  private static Resource createJurisdictionAgent(long id, String label) {
    return createResource(
      Map.of(NAME, List.of(label)),
      Set.of(JURISDICTION),
      emptyMap()
    ).setLabel(label)
      .setIdAndRefreshEdges(id);
  }

  public static Resource getWork(String titleStr, HashService hashService) {
    var titleDoc = """
      {
        "http://bibfra.me/vocab/library/mainTitle": ["%TITLE%"]
      }
      """
      .replace("%TITLE%", titleStr);
    var workDoc = """
      {
        "http://bibfra.me/vocab/library/summary": ["%SUMMARY_NOTE%"]
      }
      """
      .replace("%SUMMARY_NOTE%", titleStr + "_summary_note");
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(TEST_JSON_MAPPER.readTree(titleDoc))
      .setLabel(titleStr);
    var work = new Resource()
      .addTypes(WORK, BOOKS)
      .setDoc(TEST_JSON_MAPPER.readTree(workDoc))
      .setLabel(titleStr);

    work.addOutgoingEdge(new ResourceEdge(work, title, TITLE));

    title.setIdAndRefreshEdges(hashService.hash(title));
    work.setIdAndRefreshEdges(hashService.hash(work));

    return work;
  }
}
