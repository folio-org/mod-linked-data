package org.folio.linked.data.e2e.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CLASSIFICATION;
import static org.folio.ld.dictionary.PredicateDictionary.CONTENT;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PredicateDictionary.GOVERNMENT_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.LANGUAGE;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.ORIGIN_PLACE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.SUBJECT;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.getSampleHub;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.folio.linked.data.configuration.batch.reader.ResourceReader;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ResourceReaderIT extends ITBase {

  private static final Set<String> EXPECTED_OUTGOING_PREDICATE_URIS = Set.of(
    TITLE.getUri(), CREATOR.getUri(), CONTRIBUTOR.getUri(),
    LANGUAGE.getUri(), SUBJECT.getUri(), CLASSIFICATION.getUri(),
    MAP.getUri(), PE_PUBLICATION.getUri()
  );
  private static final Set<String> EXPECTED_INCOMING_PREDICATE_URIS = Set.of(
    INSTANTIATES.getUri()
  );

  @Autowired
  private DataSource dataSource;

  @Test
  void read_shouldReturnOnlyWorkAndHub_withCorrectEdgesAndFields() {
    // given
    var instance = tenantScopedExecutionService.execute(TENANT_ID, () ->
      resourceTestService.saveGraph(getSampleInstanceResource())
    );
    var work = instance.getOutgoingEdges().stream()
      .filter(e -> e.getPredicate().getUri().equals(INSTANTIATES.getUri()))
      .map(ResourceEdge::getTarget)
      .findFirst()
      .orElseThrow();

    var hub = tenantScopedExecutionService.execute(TENANT_ID, () ->
      resourceTestService.saveGraph(getSampleHub())
    );

    // when
    var results = tenantScopedExecutionService.execute(TENANT_ID, () -> {
      var reader = new ResourceReader(dataSource, 100, true, null);
      var list = new ArrayList<Resource>();
      reader.open(new ExecutionContext());
      Resource r;
      while ((r = reader.read()) != null) {
        list.add(r);
      }
      reader.close();
      return list;
    });

    // then
    assertThat(results).hasSize(2);
    var resultIds = results.stream().map(Resource::getId).collect(Collectors.toSet());
    assertThat(resultIds)
      .contains(work.getId(), hub.getId())
      .doesNotContain(instance.getId());

    var readWork = results.stream()
      .filter(r -> r.getId().equals(work.getId()))
      .findFirst()
      .orElseThrow();

    assertWorkFields(readWork, work);
    assertWorkOutgoingEdges(readWork);
    assertWorkIncomingEdges(readWork, instance);

    var readHub = results.stream()
      .filter(r -> r.getId().equals(hub.getId()))
      .findFirst()
      .orElseThrow();

    assertHubFields(readHub, hub);
    assertThat(readHub.getOutgoingEdges()).isEmpty();
    assertThat(readHub.getIncomingEdges()).isEmpty();
  }

  private void assertWorkFields(Resource readWork, Resource savedWork) {
    assertThat(readWork.getId()).isEqualTo(savedWork.getId());
    assertThat(readWork.getLabel()).isEqualTo(savedWork.getLabel());
    assertThat(readWork.getDoc()).isNotNull();
    assertThat(readWork.isOfType(WORK)).isTrue();

    assertThat(readWork.getIndexDate()).isNull();
    assertThat(readWork.getCreatedDate()).isNull();
    assertThat(readWork.getUpdatedDate()).isNull();
    assertThat(readWork.getCreatedBy()).isNull();
    assertThat(readWork.getUpdatedBy()).isNull();
    assertThat(readWork.getFolioMetadata()).isNull();
  }

  private void assertWorkOutgoingEdges(Resource readWork) {
    var outgoingPredicateUris = readWork.getOutgoingEdges().stream()
      .map(e -> e.getPredicate().getUri())
      .collect(Collectors.toSet());

    assertThat(outgoingPredicateUris).isSubsetOf(EXPECTED_OUTGOING_PREDICATE_URIS);

    assertThat(outgoingPredicateUris).doesNotContain(
      CONTENT.getUri(),
      GENRE.getUri(),
      GEOGRAPHIC_COVERAGE.getUri(),
      ORIGIN_PLACE.getUri(),
      DISSERTATION.getUri(),
      ILLUSTRATIONS.getUri(),
      GOVERNMENT_PUBLICATION.getUri()
    );

    assertThat(outgoingPredicateUris).contains(
      TITLE.getUri(),
      CLASSIFICATION.getUri(),
      SUBJECT.getUri(),
      LANGUAGE.getUri()
    );

    readWork.getOutgoingEdges().forEach(edge -> {
      var target = edge.getTarget();
      assertThat(target.getId()).isNotNull();
      assertThat(target.getLabel()).isNotNull();
      assertThat(target.getTypes()).isNotEmpty();
    });
  }

  private void assertWorkIncomingEdges(Resource work, Resource instance) {
    var incomingPredicateUris = work.getIncomingEdges().stream()
      .map(e -> e.getPredicate().getUri())
      .collect(Collectors.toSet());

    assertThat(incomingPredicateUris).isEqualTo(EXPECTED_INCOMING_PREDICATE_URIS);

    var incomingEdges = work.getIncomingEdges().stream()
      .filter(e -> e.getPredicate().getUri().equals(INSTANTIATES.getUri()))
      .toList();

    assertThat(incomingEdges).isNotEmpty();

    var readInstance = incomingEdges.stream()
      .map(ResourceEdge::getSource)
      .filter(s -> s.getId().equals(instance.getId()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Instance not found in incoming edges"));

    var instanceOutgoingPredicateUris = readInstance.getOutgoingEdges().stream()
      .map(e -> e.getPredicate().getUri())
      .collect(Collectors.toSet());

    assertThat(instanceOutgoingPredicateUris).isSubsetOf(EXPECTED_OUTGOING_PREDICATE_URIS);
    assertThat(instanceOutgoingPredicateUris).contains(
      TITLE.getUri(),
      PE_PUBLICATION.getUri(),
      MAP.getUri()
    );

    readInstance.getOutgoingEdges().forEach(edge -> {
      var target = edge.getTarget();
      assertThat(target.getId()).isNotNull();
      assertThat(target.getDoc()).isNotNull();
    });
  }

  private void assertHubFields(Resource readHub, Resource savedHub) {
    assertThat(readHub.getId()).isEqualTo(savedHub.getId());
    assertThat(readHub.getLabel()).isEqualTo(savedHub.getLabel());
    assertThat(readHub.getDoc()).isNotNull();
    assertThat(readHub.isOfType(HUB)).isTrue();

    assertThat(readHub.getIndexDate()).isNull();
    assertThat(readHub.getCreatedDate()).isNull();
    assertThat(readHub.getUpdatedDate()).isNull();
    assertThat(readHub.getCreatedBy()).isNull();
    assertThat(readHub.getUpdatedBy()).isNull();
    assertThat(readHub.getFolioMetadata()).isNull();
  }
}

