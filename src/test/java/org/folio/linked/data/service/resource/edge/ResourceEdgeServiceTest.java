package org.folio.linked.data.service.resource.edge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.ILLUSTRATIONS;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceEdgeServiceTest {

  @InjectMocks
  private ResourceEdgeServiceImpl resourceEdgeService;

  @Mock
  private ResourceRepository resourceRepository;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceEdgeRepository resourceEdgeRepository;

  @Test
  void saveNewResourceEdge_shouldSaveMappedEdgeResourceWithReferenceToSource() {
    // given
    var sourceId = randomLong();
    var edgeModel = new org.folio.ld.dictionary.model.ResourceEdge(
      new org.folio.ld.dictionary.model.Resource().setId(sourceId),
      new org.folio.ld.dictionary.model.Resource().setId(randomLong()), TITLE);
    var mappedEdgeResource = new org.folio.linked.data.model.entity.Resource().setId(edgeModel.getTarget().getId());
    doReturn(mappedEdgeResource).when(resourceModelMapper).toEntity(edgeModel.getTarget());
    doReturn(mappedEdgeResource).when(resourceRepository).save(mappedEdgeResource);
    when(resourceEdgeRepository.save(any(org.folio.linked.data.model.entity.ResourceEdge.class)))
      .thenAnswer(i -> i.getArguments()[0]);

    // when
    var result = resourceEdgeService.saveNewResourceEdge(sourceId, edgeModel);

    // then
    assertThat(result.getSourceHash()).isEqualTo(edgeModel.getSource().getId());
    assertThat(result.getTargetHash()).isEqualTo(edgeModel.getTarget().getId());
    assertThat(result.getPredicateHash()).isEqualTo(edgeModel.getPredicate().getHash());
  }

  static Stream<Arguments> dataProvider() {
    return Stream.of(
      Arguments.of(getInstance(), getEmptyInstance(), List.of(ADMIN_METADATA.getUri())),
      Arguments.of(getWork(), getEmptyWork(), List.of(ILLUSTRATIONS.getUri(),
        DISSERTATION.getUri(), GENRE.getUri())),
      Arguments.of(getInstance(), getEmptyWork(), List.of()),
      Arguments.of(getWork(), getEmptyInstance(), List.of())
    );
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void copyOutgoingEdges_shouldCopy_appropriateEdges(Resource from,
                                                     Resource to,
                                                     List<String> expectedPredicates) {
    //when
    resourceEdgeService.copyOutgoingEdges(from, to);

    //then
    assertThat(to.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getPredicate)
      .map(PredicateEntity::getUri)
      .toList()
    ).containsExactlyInAnyOrderElementsOf(expectedPredicates);
  }

  private static Resource getEmptyInstance() {
    var instance = new Resource();
    instance.setTypes(Set.of(new ResourceTypeEntity(1L, ResourceTypeDictionary.INSTANCE.getUri(), "instance")));
    return instance;
  }

  private static Resource getInstance() {
    var instance = getEmptyInstance();
    instance.addOutgoingEdge(new ResourceEdge(instance, new Resource(), TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, new Resource(), ADMIN_METADATA));
    return instance;
  }

  private static Resource getEmptyWork() {
    var work = new Resource();
    work.setTypes(Set.of(new ResourceTypeEntity(2L, ResourceTypeDictionary.WORK.getUri(), "work")));
    return work;
  }

  private static Resource getWork() {
    var work = getEmptyWork();
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), TITLE));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), ILLUSTRATIONS));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), DISSERTATION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), GENRE));
    return work;
  }
}
