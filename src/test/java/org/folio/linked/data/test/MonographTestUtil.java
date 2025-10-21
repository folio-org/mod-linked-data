package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.EXTENT;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.GRANTING_INSTITUTION;
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
import static org.folio.ld.dictionary.PropertyDictionary.DEGREE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_ID;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.DISSERTATION_YEAR;
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
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
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
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_IAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LANGUAGE_CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.TestUtil.readTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  public static Resource getSampleInstanceResource() {
    return getSampleInstanceResource(null, getSampleWork(null));
  }

  public static Resource getSampleInstanceResource(Long id) {
    return getSampleInstanceResource(id, getSampleWork(null));
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

    return createResource(
      Map.of(
        PART_NAME, List.of("Primary: partName"),
        PART_NUMBER, List.of("Primary: partNumber"),
        MAIN_TITLE, List.of(primaryTitleValue),
        NON_SORT_NUM, List.of("Primary: nonSortNum"),
        SUBTITLE, List.of(subTitleValue)
      ),
      Set.of(ResourceTypeDictionary.TITLE),
      emptyMap()
    ).setLabel(primaryTitleValue + " " + subTitleValue);
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
        NAME, List.of("genre 1"),
        RESOURCE_PREFERRED, List.of("true")
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
    pred2OutgoingResources.put(DISSERTATION, List.of(createDissertation()));
    pred2OutgoingResources.put(LANGUAGE, List.of(language));
    pred2OutgoingResources.put(ILLUSTRATIONS, List.of(createIllustrations()));
    pred2OutgoingResources.put(PredicateDictionary.SUPPLEMENTARY_CONTENT, List.of(createSupplementaryContent()));

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

  public static Resource getSubjectPersonPreferred() {
    return getSubjectAndConcept("person", -6999093488677112301L, -3951421359442339069L, PERSON, true);
  }

  public static Resource getSubjectFormNotPreferred() {
    return getSubjectAndConcept("form", -4718450084121784027L, -354125450028352284L, FORM, false);
  }

  private static Resource getSubjectAndConcept(String name,
                                               Long subjectId,
                                               Long conceptId,
                                               ResourceTypeDictionary type,
                                               boolean isPreferred) {
    var subject = createResource(
      isPreferred
        ? Map.of(NAME, List.of("Subject " + name), RESOURCE_PREFERRED, List.of("true"))
        : Map.of(NAME, List.of("Subject " + name)),
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

  private static Resource createDissertation() {
    var grantingInstitution1 = createResource(
      Map.of(
        NAME, List.of("granting institution 1")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("granting institution 1")
      .setIdAndRefreshEdges(5481852630377445080L);

    var grantingInstitution2 = createResource(
      Map.of(
        NAME, List.of("granting institution 2")
      ),
      Set.of(ORGANIZATION),
      emptyMap()
    ).setLabel("granting institution 2")
      .setIdAndRefreshEdges(-6468470931408362304L);

    return createResource(
      Map.of(
        LABEL, List.of("label"),
        DEGREE, List.of("degree"),
        DISSERTATION_YEAR, List.of("dissertation year"),
        DISSERTATION_NOTE, List.of("dissertation note"),
        DISSERTATION_ID, List.of("dissertation id")
      ),
      Set.of(ResourceTypeDictionary.DISSERTATION),
      Map.of(
        GRANTING_INSTITUTION, List.of(grantingInstitution1, grantingInstitution2)
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
      .setDoc(readTree(titleDoc))
      .setLabel(titleStr);
    var work = new Resource()
      .addTypes(WORK, BOOKS)
      .setDoc(readTree(workDoc))
      .setLabel(titleStr);

    work.addOutgoingEdge(new ResourceEdge(work, title, TITLE));

    title.setIdAndRefreshEdges(hashService.hash(title));
    work.setIdAndRefreshEdges(hashService.hash(work));

    return work;
  }
}
