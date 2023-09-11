package org.folio.linked.data.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Bibframe2Constants.EDITION_STATEMENT_URL;
import static org.folio.linked.data.util.Bibframe2Constants.IDENTIFIED_BY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PROVISION_ACTIVITY_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.PUBLICATION;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_AGENT_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SIMPLE_DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.VALUE_PRED;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.ProfiledMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.search.domain.dto.BibframeIdentifiersInner;
import org.folio.spring.test.type.UnitTest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BibframeMapperTest {

  private BibframeMapper bibframeMapper;
  @Mock
  private ProfiledMapper profiledMapper;

  @BeforeEach
  void setUp() {
    bibframeMapper = new BibframeMapperImpl();
    ReflectionTestUtils.setField(bibframeMapper, "profiledMapper", profiledMapper);
  }

  @Test
  void map_shouldFillEdgesPk() {
    // given
    var dto = new Bibframe2Request();
    var re1 = new ResourceEdge(new Resource().setResourceHash(111L), new Resource().setResourceHash(222L),
      new Predicate().setPredicateHash(333L));
    var re2 = new ResourceEdge(new Resource().setResourceHash(444L), new Resource().setResourceHash(555L),
      new Predicate().setPredicateHash(666L));
    var re3 = new ResourceEdge(new Resource().setResourceHash(777L), new Resource().setResourceHash(888L),
      new Predicate().setPredicateHash(999L));
    re2.getTarget().getOutgoingEdges().add(re3);

    var expectedResource = new Resource();
    expectedResource.getOutgoingEdges().add(re1);
    expectedResource.getOutgoingEdges().add(re2);
    doReturn(expectedResource).when(profiledMapper).toEntity(dto);

    // when
    var resource = bibframeMapper.toEntity2(dto);

    // then
    var resourceEdgeIterator = resource.getOutgoingEdges().iterator();
    var result1 = resourceEdgeIterator.next();
    assertThat(result1.getId().getSourceHash()).isEqualTo(re1.getSource().getResourceHash());
    assertThat(result1.getId().getTargetHash()).isEqualTo(re1.getTarget().getResourceHash());
    assertThat(result1.getId().getPredicateHash()).isEqualTo(re1.getPredicate().getPredicateHash());
    var result2 = resourceEdgeIterator.next();
    assertThat(result2.getId().getSourceHash()).isEqualTo(re2.getSource().getResourceHash());
    assertThat(result2.getId().getTargetHash()).isEqualTo(re2.getTarget().getResourceHash());
    assertThat(result2.getId().getPredicateHash()).isEqualTo(re2.getPredicate().getPredicateHash());
    var result3 = result2.getTarget().getOutgoingEdges().iterator().next();
    assertThat(result3.getId().getSourceHash()).isEqualTo(re3.getSource().getResourceHash());
    assertThat(result3.getId().getTargetHash()).isEqualTo(re3.getTarget().getResourceHash());
    assertThat(result3.getId().getPredicateHash()).isEqualTo(re3.getPredicate().getPredicateHash());
  }

  @Test
  void mapToIndex_shouldThrowNullPointerException_ifGivenResourceIsNull() {
    // given
    Resource resource = null;

    // when
    var thrown = assertThrows(NullPointerException.class, () -> bibframeMapper.mapToIndex2(resource));

    // then
    MatcherAssert.assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void mapToIndex_shouldThrowNotSupportedException_ifGivenResourceContainsNoInstance() {
    // given
    var resource = new Resource();

    // when
    var thrown = assertThrows(NotSupportedException.class, () -> bibframeMapper.mapToIndex2(resource));

    // then
    MatcherAssert.assertThat(thrown.getMessage(),
      is("Only Monograph.Instance bibframe is supported for now, and there is no Instance found"));
  }

  @Test
  void mapToIndex_shouldReturnCorrectlyMappedObject() {
    // given
    var resource = new Resource();
    resource.setResourceHash(randomLong());
    var instance = new Resource();
    resource.getOutgoingEdges().add(new ResourceEdge(resource, instance, new Predicate(INSTANCE_URL)));
    instance.setLabel(UUID.randomUUID().toString());
    var identifier = new Resource();
    identifier.setResourceHash(randomLong());
    identifier.setDoc(getJsonNode(Map.of(VALUE_PRED, List.of(randomLong()))));
    identifier.addType(new ResourceType().setSimpleLabel(random(BibframeIdentifiersInner.TypeEnum.class).getValue()));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, identifier, new Predicate(IDENTIFIED_BY_PRED)));
    var publication = new Resource();
    publication.setResourceHash(randomLong());
    publication.setDoc(
      getJsonNode(Map.of(SIMPLE_DATE_PRED, List.of("2023"), SIMPLE_AGENT_PRED, List.of(UUID.randomUUID().toString()))));
    publication.addType(new ResourceType().setSimpleLabel(PUBLICATION));
    instance.getOutgoingEdges().add(new ResourceEdge(instance, publication, new Predicate(PROVISION_ACTIVITY_PRED)));
    instance.setDoc(getJsonNode(Map.of(EDITION_STATEMENT_URL, List.of(UUID.randomUUID().toString()))));

    // when
    var result = bibframeMapper.mapToIndex2(resource);

    // then
    assertThat(result.getId()).isEqualTo(resource.getResourceHash().toString());
    assertThat(result.getTitle()).isEqualTo(instance.getLabel());
    assertThat(result.getIdentifiers().get(0).getValue()).isEqualTo(
      identifier.getDoc().get(VALUE_PRED).get(0).textValue());
    assertThat(result.getIdentifiers().get(0).getType()).isEqualTo(
      BibframeIdentifiersInner.TypeEnum.fromValue(identifier.getLastType().getSimpleLabel()));
    assertThat(result.getPublications().get(0).getDateOfPublication()).isEqualTo(
      publication.getDoc().get(SIMPLE_DATE_PRED).get(0).textValue());
    assertThat(result.getPublications().get(0).getPublisher()).isEqualTo(
      publication.getDoc().get(SIMPLE_AGENT_PRED).get(0).textValue());
    assertThat(result.getEditionStatement()).isEqualTo(
      instance.getDoc().get(EDITION_STATEMENT_URL).get(0).textValue());
  }

}
