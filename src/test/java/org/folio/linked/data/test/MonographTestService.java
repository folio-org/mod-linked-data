package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PredicateDictionary.CARRIER;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.COPYRIGHT;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.MEDIA;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.STATUS;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.DIMENSIONS;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.EXTENT;
import static org.folio.ld.dictionary.PropertyDictionary.ISSUANCE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PropertyDictionary.LCNAF_ID;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.PROJECTED_PROVISION_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.PropertyDictionary.RESPONSIBILITY_STATEMENT;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.PropertyDictionary.SUMMARY;
import static org.folio.ld.dictionary.PropertyDictionary.TABLE_OF_CONTENTS;
import static org.folio.ld.dictionary.PropertyDictionary.TARGET_AUDIENCE;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.PropertyDictionary.VARIANT_TYPE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.COPYRIGHT_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PARALLEL_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.VARIANT_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.getJsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MonographTestService {

  private final CoreMapper coreMapper;

  public ResourceTypeEntity getInstanceType() {
    return new ResourceTypeEntity(INSTANCE.getHash(), INSTANCE.getUri(), null);
  }

  public Resource createSampleInstance() {
    var instanceTitle = createResource(
      Map.of(
        PART_NAME, List.of("Instance: partName"),
        PART_NUMBER, List.of("Instance: partNumber"),
        MAIN_TITLE, List.of("Instance: mainTitle"),
        NON_SORT_NUM, List.of("Instance: nonSortNum"),
        SUBTITLE, List.of("Instance: subTitle")
      ),
      ResourceTypeDictionary.TITLE,
      emptyMap()
    ).setLabel("Instance: label");

    var parallelTitle = createResource(
      Map.of(
        PART_NAME, List.of("Parallel: partName"),
        PART_NUMBER, List.of("Parallel: partNumber"),
        MAIN_TITLE, List.of("Parallel: mainTitle"),
        DATE, List.of("Parallel: date"),
        SUBTITLE, List.of("Parallel: subTitle"),
        NOTE, List.of("Parallel: noteLabel")
      ),
      PARALLEL_TITLE,
      emptyMap()
    ).setLabel("Parallel: label");

    var variantTitle = createResource(
      Map.of(
        PART_NAME, List.of("Variant: partName"),
        PART_NUMBER, List.of("Variant: partNumber"),
        MAIN_TITLE, List.of("Variant: mainTitle"),
        DATE, List.of("Variant: date"),
        SUBTITLE, List.of("Variant: subTitle"),
        VARIANT_TYPE, List.of("Variant: variantType"),
        NOTE, List.of("Variant: noteLabel")
      ),
      VARIANT_TITLE,
      emptyMap()
    ).setLabel("Variant: label");

    var production = providerEvent("production");
    var publication = providerEvent("publication");
    var distribution = providerEvent("distribution");
    var manufacture = providerEvent("manufacture");

    var accessLocation = createResource(
      Map.of(
        LINK, List.of("accessLocation value"),
        NOTE, List.of("accessLocation note")
      ),
      ANNOTATION,
      emptyMap()
    ).setLabel("accessLocation label");

    var lccn = createResource(
      Map.of(NAME, List.of("lccn value")),
      ID_LCCN,
      Map.of(STATUS, List.of(status("lccn")))
    ).setLabel("lccn label");

    var isbn = createResource(
      Map.of(
        NAME, List.of("isbn value"),
        QUALIFIER, List.of("isbn qualifier")
      ),
      ID_ISBN,
      Map.of(STATUS, List.of(status("isbn")))
    ).setLabel("isbn label");

    var ean = createResource(
      Map.of(
        EAN_VALUE, List.of("ean value"),
        QUALIFIER, List.of("ean qualifier")
      ),
      ID_EAN,
      emptyMap()
    ).setLabel("ean label");

    var localId = createResource(
      Map.of(
        LOCAL_ID_VALUE, List.of("localId value"),
        ASSIGNING_SOURCE, List.of("localId assigner")
      ),
      ID_LOCAL,
      emptyMap()
    ).setLabel("localId label");

    var otherId = createResource(
      Map.of(
        NAME, List.of("otherId value"),
        QUALIFIER, List.of("otherId qualifier")
      ),
      ID_UNKNOWN,
      emptyMap()
    ).setLabel("otherId label");

    var media = createResource(
      Map.of(
        CODE, List.of("media code"),
        TERM, List.of("media term"),
        LINK, List.of("media link")
      ),
      CATEGORY,
      emptyMap()
    ).setLabel("media label");

    var carrier = createResource(
      Map.of(
        CODE, List.of("carrier code"),
        TERM, List.of("carrier term"),
        LINK, List.of("carrier link")
      ),
      CATEGORY,
      emptyMap()
    ).setLabel("carrier label");

    var copyrightEvent = createResource(
      Map.of(
        DATE, List.of("copyright date value")
      ),
      COPYRIGHT_EVENT,
      emptyMap()
    ).setLabel("copyright date label");

    var pred2OutgoingResources = new LinkedHashMap<PredicateDictionary, List<Resource>>();
    pred2OutgoingResources.put(TITLE, List.of(instanceTitle, parallelTitle, variantTitle));
    pred2OutgoingResources.put(PE_PRODUCTION, List.of(production));
    pred2OutgoingResources.put(PE_PUBLICATION, List.of(publication));
    pred2OutgoingResources.put(PE_DISTRIBUTION, List.of(distribution));
    pred2OutgoingResources.put(PE_MANUFACTURE, List.of(manufacture));
    pred2OutgoingResources.put(ACCESS_LOCATION, List.of(accessLocation));
    pred2OutgoingResources.put(MAP, List.of(lccn, isbn, ean, localId, otherId));
    pred2OutgoingResources.put(MEDIA, List.of(media));
    pred2OutgoingResources.put(CARRIER, List.of(carrier));
    pred2OutgoingResources.put(COPYRIGHT, List.of(copyrightEvent));
    pred2OutgoingResources.put(INSTANTIATES, List.of(createSampleWork()));

    return createResource(
      Map.of(
        EXTENT, List.of("extent info"),
        DIMENSIONS, List.of("20 cm"),
        RESPONSIBILITY_STATEMENT, List.of("responsibility statement"),
        EDITION_STATEMENT, List.of("edition statement"),
        PROJECTED_PROVISION_DATE, List.of("projected provision date"),
        ISSUANCE, List.of("single unit")
      ),
      INSTANCE,
      pred2OutgoingResources
    );
  }

  public Resource createSampleWork() {
    var content = createResource(
      Map.of(
        TERM, List.of("Content: term"),
        LINK, List.of("Content: link"),
        CODE, List.of("Content: code")
      ),
      CATEGORY,
      emptyMap()
    ).setLabel("content label");

    var deweyClassification = createResource(
      Map.of(
        CODE, List.of("Dewey: code"),
        SOURCE, List.of("Dewey: source")
      ),
      CATEGORY,
      emptyMap()
    ).setLabel("Dewey: label");

    var person = createResource(
      Map.of(
        NAME, List.of("Person: name"),
        LCNAF_ID, List.of("Person: lcnafId")
      ),
      PERSON,
      emptyMap()
    );

    var organization = createResource(
      Map.of(
        NAME, List.of("Organization: name"),
        LCNAF_ID, List.of("Organization: lcnafId")
      ),
      ORGANIZATION,
      emptyMap()
    );

    return createResource(
      Map.of(
        TARGET_AUDIENCE, List.of("Work: target audience"),
        LANGUAGE, List.of("Work: language"),
        SUMMARY, List.of("Work: summary"),
        TABLE_OF_CONTENTS, List.of("Work: table of contents")
      ),
      WORK,
      Map.of(
        CLASSIFICATION, List.of(deweyClassification),
        CREATOR, List.of(person),
        CONTRIBUTOR, List.of(organization),
        CONTENT, List.of(content)
      )
    ).setLabel("Work: label");
  }

  private Resource status(String prefix) {
    return createResource(
      Map.of(
        LABEL, List.of(prefix + " status value"),
        LINK, List.of(prefix + " status link")
      ),
      ResourceTypeDictionary.STATUS,
      emptyMap()
    ).setLabel(prefix + " status label");
  }

  private Resource providerEvent(String type) {
    return createResource(
      Map.of(
        DATE, List.of(type + " date"),
        NAME, List.of(type + " name"),
        PROVIDER_DATE, List.of(type + " provider date"),
        SIMPLE_PLACE, List.of(type + " simple place")
      ),
      PROVIDER_EVENT,
      Map.of(PROVIDER_PLACE, List.of(providerPlace(type)))
    ).setLabel(type + " label");
  }

  private Resource providerPlace(String providerEventType) {
    return createResource(
      Map.of(
        NAME, List.of(providerEventType + " providerPlace name"),
        LINK, List.of(providerEventType + " providerPlace link")
      ),
      ResourceTypeDictionary.PLACE,
      emptyMap()
    ).setLabel(providerEventType + " providerPlace label");
  }

  private Resource createResource(Map<PropertyDictionary, List<String>> propertiesDic, ResourceTypeDictionary type,
                                  Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    var resource = new Resource();
    pred2OutgoingResources.keySet()
      .stream()
      .flatMap(pred -> pred2OutgoingResources.get(pred)
        .stream()
        .map(target -> new ResourceEdge(resource, target, pred)))
      .forEach(edge -> resource.getOutgoingEdges().add(edge));

    Map<String, List<String>> properties = propertiesDic.entrySet().stream()
      .collect(Collectors.toMap(e -> e.getKey().getValue(), Map.Entry::getValue));
    resource.setDoc(getJsonNode(properties));
    resource.addType(type);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

}
