package org.folio.linked.data.test;

import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.APPLICABLE_INSTITUTION_URL;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_PRED;
import static org.folio.linked.data.util.BibframeConstants.CARRIER_URL;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DIMENSIONS_URL;
import static org.folio.linked.data.util.BibframeConstants.DISTRIBUTION;
import static org.folio.linked.data.util.BibframeConstants.EXTENT;
import static org.folio.linked.data.util.BibframeConstants.EXTENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_EAN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_ISBN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LCCN;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_LOCAL;
import static org.folio.linked.data.util.BibframeConstants.IDENTIFIERS_OTHER;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION;
import static org.folio.linked.data.util.BibframeConstants.IMM_ACQUISITION_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ISSUANCE_URL;
import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MANUFACTURE;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEDIA_URL;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_URL;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PERSON;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PRODUCTION;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_ID;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_LABEL;
import static org.folio.linked.data.util.BibframeConstants.PROPERTY_URI;
import static org.folio.linked.data.util.BibframeConstants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.BibframeConstants.PUBLICATION;
import static org.folio.linked.data.util.BibframeConstants.ROLE;
import static org.folio.linked.data.util.BibframeConstants.ROLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.ROLE_URL;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.exception.NotSupportedException;
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

  public ResourceType getMonographProfile() {
    return resourceTypeService.get(MONOGRAPH);
  }

  public Resource createSampleMonograph() {
    var instance = createSampleInstance();
    return createResource(
      Collections.emptyMap(),
      MONOGRAPH,
      Map.of(INSTANCE_URL, List.of(instance))
    );
  }

  private Resource createSampleInstance() {
    var instanceTitle = title("Instance: ", INSTANCE_TITLE);
    var parallelTitle = title("Parallel: ", PARALLEL_TITLE);
    var variantTitle = title("Variant: ", VARIANT_TITLE);

    var distribution = provisionActivity("Distribution: ", DISTRIBUTION);
    var manufacture = provisionActivity("Manufacture: ", MANUFACTURE);
    var production = provisionActivity("Production: ", PRODUCTION);
    var publication = provisionActivity("Publication: ", PUBLICATION);

    var person = createResource(Map.of(
        SAME_AS_PRED, List.of(Map.of(
          PROPERTY_LABEL, "Test and Evaluation Year-2000 Team (U.S.)",
          PROPERTY_URI, "http://id.loc.gov/authorities/names/no98072015")
        )
      ), PERSON,
      Collections.emptyMap());

    var role = createSimpleResource(
      "Author",
      ROLE,
      ROLE_URL
    );

    var contrib = createResource(
      Collections.emptyMap(),
      CONTRIBUTION,
      Map.of(
        AGENT_PRED, List.of(person),
        ROLE_PRED, List.of(role)
      )
    );

    var ean = identifiedBy(IDENTIFIERS_EAN, "12345670");
    var isbn = identifiedBy(IDENTIFIERS_ISBN, "12345671");
    var lccn = identifiedBy(IDENTIFIERS_LCCN, "12345672");
    var local = identifiedBy(IDENTIFIERS_LOCAL, "12345673");
    var other = identifiedBy(IDENTIFIERS_OTHER, "12345674");

    var note = createSimpleResource(
      "some note",
      NOTE,
      NOTE_URL
    );

    var extent = createResource(
      Map.of(LABEL_PRED, List.of("vi, 374 pages, 4 unnumbered leaves of plates")),
      EXTENT,
      Collections.emptyMap()
    );

    var issuance = createSimpleResource(
      "single unit",
      null,
      ISSUANCE_URL
    );

    var carrier = createSimpleResource(
      "volume",
      null,
      CARRIER_URL
    );

    var media = createSimpleResource(
      "unmediated",
      null,
      MEDIA_URL
    );

    var immediateAcquisition = createResource(
      Map.of(
        APPLICABLE_INSTITUTION_PRED, List.of(Map.of(
          PROPERTY_LABEL, "some applicableInstitution",
          PROPERTY_URI, APPLICABLE_INSTITUTION_URL
        )),
        LABEL_PRED, List.of("some immediateAcquisition")
      ),
      IMM_ACQUISITION,
      Collections.emptyMap()
    );

    return createResource(
      Map.of(DIMENSIONS_URL, List.of("20 cm")),
      INSTANCE,
      Map.of(
        INSTANCE_TITLE_PRED, List.of(instanceTitle, parallelTitle, variantTitle),
        PROVISION_ACTIVITY_PRED, List.of(distribution, manufacture, production, publication),
        CONTRIBUTION_PRED, List.of(contrib),
        IDENTIFIED_BY_PRED, List.of(ean, isbn, lccn, local, other),
        NOTE_PRED, List.of(note),
        EXTENT_PRED, List.of(extent),
        ISSUANCE_PRED, List.of(issuance),
        CARRIER_PRED, List.of(carrier),
        MEDIA_PRED, List.of(media),
        IMM_ACQUISITION_PRED, List.of(immediateAcquisition)
      )
    );
  }

  private Resource title(String prefix, String url) {
    return createResource(
      Map.of(MAIN_TITLE_PRED, List.of(prefix + "Laramie holds the range")),
      url,
      Collections.emptyMap()
    );
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
      Map.of(PLACE_PRED, List.of(place(prefix)))
    );
  }

  private Resource place(String prefix) {
    return createSimpleResource(
      prefix + "New York (State)",
      "lc:RT:bf2:Place",
      "http://id.loc.gov/ontologies/bibframe/Place"
    );
  }

  private Resource identifiedBy(String type, String value) {
    return createResource(
      Map.of(VALUE_URL, List.of(value)),
      type,
      Collections.emptyMap()
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
    resource.setResourceHash(coreMapper.hash(resource));
    resource.setType(resourceTypeService.get(typeLabel));
    return resource;
  }

  private Resource createSimpleResource(String label, String typeLabel, String typeUri) {
    var resource = new Resource();

    var map = new HashMap<>(Map.of(PROPERTY_URI, typeUri, PROPERTY_LABEL, label));
    if (StringUtils.isNoneBlank(typeLabel)) {
      map.put(PROPERTY_ID, typeLabel);
      resource.setType(findTypeByLabelOfUri(typeLabel, typeUri));
    } else {
      resource.setType(resourceTypeService.get(typeUri));
    }
    var doc = getJsonNode(map);
    resource.setDoc(doc);
    resource.setResourceHash(coreMapper.hash(resource));
    resource.setLabel(label);

    return resource;
  }

  private ResourceType findTypeByLabelOfUri(String label, String uri) {
    try {
      return resourceTypeService.get(label);
    } catch (NotSupportedException nse) {
      return resourceTypeService.get(uri);
    }
  }

}
