package org.folio.linked.data.test;

import static java.util.Collections.emptyMap;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.ANNOTATION;
import static org.folio.linked.data.util.BibframeConstants.ASSIGNING_SOURCE;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CATEGORY;
import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.CODE;
import static org.folio.linked.data.util.BibframeConstants.CONTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_EVENT;
import static org.folio.linked.data.util.BibframeConstants.COPYRIGHT_PRED;
import static org.folio.linked.data.util.BibframeConstants.CREATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.EAN;
import static org.folio.linked.data.util.BibframeConstants.EAN_VALUE;
import static org.folio.linked.data.util.BibframeConstants.EDITION_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANTIATES_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISBN;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE;
import static org.folio.linked.data.util.BibframeConstants.LABEL;
import static org.folio.linked.data.util.BibframeConstants.LANGUAGE;
import static org.folio.linked.data.util.BibframeConstants.LCCN;
import static org.folio.linked.data.util.BibframeConstants.LCNAF_ID;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.ORGANIZATION;
import static org.folio.linked.data.util.BibframeConstants.OTHER_ID;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.PERSON;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROJECTED_PROVISION_DATE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_DATE;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.QUALIFIER;
import static org.folio.linked.data.util.BibframeConstants.RESPONSIBILITY_STATEMENT;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE;
import static org.folio.linked.data.util.BibframeConstants.SOURCE;
import static org.folio.linked.data.util.BibframeConstants.STATUS;
import static org.folio.linked.data.util.BibframeConstants.STATUS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeConstants.SUMMARY;
import static org.folio.linked.data.util.BibframeConstants.TABLE_OF_CONTENTS;
import static org.folio.linked.data.util.BibframeConstants.TARGET_AUDIENCE;
import static org.folio.linked.data.util.BibframeConstants.TERM;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TYPE;
import static org.folio.linked.data.util.BibframeConstants.WORK;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MonographTestService {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final DictionaryService<Predicate> predicateService;
  private final CoreMapper coreMapper;

  public ResourceType getInstanceType() {
    return resourceTypeService.get(INSTANCE);
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
      INSTANCE_TITLE,
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
      LCCN,
      Map.of(STATUS_PRED, List.of(status("lccn")))
    ).setLabel("lccn label");

    var isbn = createResource(
      Map.of(
        NAME, List.of("isbn value"),
        QUALIFIER, List.of("isbn qualifier")
      ),
      ISBN,
      Map.of(STATUS_PRED, List.of(status("isbn")))
    ).setLabel("isbn label");

    var ean = createResource(
      Map.of(
        EAN_VALUE, List.of("ean value"),
        QUALIFIER, List.of("ean qualifier")
      ),
      EAN,
      emptyMap()
    ).setLabel("ean label");

    var localId = createResource(
      Map.of(
        LOCAL_ID_VALUE, List.of("localId value"),
        ASSIGNING_SOURCE, List.of("localId assigner")
      ),
      LOCAL_ID,
      emptyMap()
    ).setLabel("localId label");

    var otherId = createResource(
      Map.of(
        NAME, List.of("otherId value"),
        QUALIFIER, List.of("otherId qualifier")
      ),
      OTHER_ID,
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
    pred2OutgoingResources.put(COPYRIGHT_PRED, List.of(copyrightEvent));
    pred2OutgoingResources.put(INSTANTIATES_PRED, List.of(createSampleWork()));

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
        CLASSIFICATION_PRED, List.of(deweyClassification),
        CREATOR_PRED, List.of(person),
        CONTRIBUTOR_PRED, List.of(organization),
        CONTENT_PRED, List.of(content)
      )
    ).setLabel("Work: label");
  }

  private Resource status(String prefix) {
    return createResource(
      Map.of(
        LABEL, List.of(prefix + " status value"),
        LINK, List.of(prefix + " status link")
      ),
      STATUS,
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
      Map.of(PROVIDER_PLACE_PRED, List.of(providerPlace(type)))
    ).setLabel(type + " label");
  }

  private Resource providerPlace(String providerEventType) {
    return createResource(
      Map.of(
        NAME, List.of(providerEventType + " providerPlace name"),
        LINK, List.of(providerEventType + " providerPlace link")
      ),
      PLACE,
      emptyMap()
    ).setLabel(providerEventType + " providerPlace label");
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

}
