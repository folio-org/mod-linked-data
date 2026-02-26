package org.folio.linked.data.mapper.kafka.search;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.EDITION;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.ORGANIZATION;
import static org.folio.linked.data.domain.dto.LinkedDataContributor.TypeEnum.PERSON;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.IAN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.ISBN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.LCCN;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.UNKNOWN;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN_PARALLEL;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.MAIN_VARIANT;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB_PARALLEL;
import static org.folio.linked.data.domain.dto.LinkedDataTitle.TypeEnum.SUB_VARIANT;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.LinkedDataContributor;
import org.folio.linked.data.domain.dto.LinkedDataIdentifier;
import org.folio.linked.data.domain.dto.LinkedDataInstanceOnly;
import org.folio.linked.data.domain.dto.LinkedDataTitle;
import org.folio.linked.data.domain.dto.LinkedDataWork;
import org.folio.linked.data.mapper.dto.resource.common.NoteMapper;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WorkSearchMessageMapperTest {

  @InjectMocks
  private WorkSearchMessageMapperImpl workSearchMessageMapper;

  @Mock
  private NoteMapper noteMapper;
  @Spy
  private IndexIdentifierMapper innerIndexIdentifierMapper = new IndexIdentifierMapperImpl();

  @Test
  void toIndex_shouldReturnCorrectlyMappedIndex_fromResourceWithIdOnly() {
    // given
    var resource = new Resource().setIdAndRefreshEdges(randomLong());

    // when
    var result = workSearchMessageMapper.toIndex(resource, CREATE);

    // then
    assertThat(result)
      .hasAllNullFieldsOrPropertiesExcept("id", "resourceName", "type", "_new")
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("type", CREATE)
      .extracting("_new")
      .isInstanceOf(LinkedDataWork.class);
    var linkedDataWork = (LinkedDataWork) result.getNew();
    assertThat(linkedDataWork)
      .hasFieldOrPropertyWithValue("id", String.valueOf(resource.getId()))
      .hasFieldOrPropertyWithValue("titles", emptyList())
      .hasFieldOrPropertyWithValue("contributors", emptyList())
      .hasFieldOrPropertyWithValue("hubAAPs", emptyList())
      .hasFieldOrPropertyWithValue("languages", emptyList())
      .hasFieldOrPropertyWithValue("notes", emptyList())
      .hasFieldOrPropertyWithValue("classifications", emptyList())
      .hasFieldOrPropertyWithValue("subjects", emptyList())
      .hasFieldOrPropertyWithValue("instances", emptyList());
  }

  @Test
  void toIndex_shouldReturnCorrectlyMappedIndex_fromWork() {
    // given
    var work = getSampleWork();
    var wrongContributor = getContributor(ANNOTATION, "wrong contributor");
    var emptyContributor = new Resource();
    var creatorPerson = getContributor(ResourceTypeDictionary.PERSON, "creator person");
    var contributorOrg = getContributor(ResourceTypeDictionary.ORGANIZATION, "contributor organization");
    work.addOutgoingEdge(new ResourceEdge(work, creatorPerson, CREATOR));
    work.addOutgoingEdge(new ResourceEdge(work, contributorOrg, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, wrongContributor, CONTRIBUTOR));
    work.addOutgoingEdge(new ResourceEdge(work, emptyContributor, CONTRIBUTOR));
    final var instance1 = getInstance(1L, work);
    final var instance2 = getInstance(2L, work);
    final var emptyInstance = new Resource().setIdAndRefreshEdges(3L).addTypes(INSTANCE);
    var edge = new ResourceEdge(emptyInstance, work, INSTANTIATES);
    emptyInstance.addOutgoingEdge(edge);
    work.addIncomingEdge(edge);

    // when
    var result = workSearchMessageMapper.toIndex(work, CREATE);

    // then
    assertThat(result)
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-work")
      .hasFieldOrPropertyWithValue("type", CREATE)
      .extracting("_new")
      .isInstanceOf(LinkedDataWork.class);
    var linkedDataWork = (LinkedDataWork) result.getNew();
    validateWork(linkedDataWork, work, 2);
    assertContributor(linkedDataWork.getContributors().getFirst(), creatorPerson.getLabel(), PERSON, true);
    assertContributor(linkedDataWork.getContributors().get(1), contributorOrg.getLabel(), ORGANIZATION, false);
    assertContributor(linkedDataWork.getContributors().get(2), wrongContributor.getLabel(), null, false);
    assertThat(linkedDataWork.getContributors()).hasSize(3);
    validateInstance(linkedDataWork.getInstances().getFirst(), instance1);
    validateInstance(linkedDataWork.getInstances().get(1), instance2);
  }

  private Resource getInstance(Long id, Resource work) {
    var instance = getSampleInstanceResource(id, work);
    var emptyTitle = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyTitle, TITLE));
    var wrongId = getIdentifier();
    var emptyId = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, wrongId, MAP));
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyId, MAP));
    var emptyPublication = new Resource();
    instance.addOutgoingEdge(new ResourceEdge(instance, emptyPublication, PE_PUBLICATION));
    return instance;
  }

  private Resource getIdentifier() {
    var id = new Resource();
    id.setIdAndRefreshEdges(randomLong());
    id.setDoc(getJsonNode(Map.of(NAME.getValue(), List.of("wrongId"))));
    id.addTypes(ANNOTATION);
    return id;
  }

  private Resource getContributor(ResourceTypeDictionary type, String label) {
    return new Resource()
      .setIdAndRefreshEdges(randomLong())
      .addTypes(type)
      .setLabel(label);
  }

  private void validateWork(LinkedDataWork result, Resource work, int instancesExpected) {
    assertThat(result.getId()).isEqualTo(work.getId().toString());
    assertTitle(result.getTitles().getFirst(), "Primary: mainTitle", MAIN);
    assertTitle(result.getTitles().get(1), "Primary: subTitle", SUB);
    assertTitle(result.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(result.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(result.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(result.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
    assertThat(result.getLanguages()).hasSize(1);
    assertThat(result.getLanguages().getFirst()).isEqualTo("eng");
    assertThat(result.getClassifications()).hasSize(2);
    assertThat(result.getClassifications().getFirst().getType()).isEqualTo("lc");
    assertThat(result.getClassifications().getFirst().getNumber()).isEqualTo("lc code");
    assertThat(result.getClassifications().getFirst().getAdditionalNumber()).isEqualTo("lc item number");
    assertThat(result.getClassifications().get(1).getType()).isEqualTo("ddc");
    assertThat(result.getClassifications().get(1).getNumber()).isEqualTo("ddc code");
    assertThat(result.getClassifications().get(1).getAdditionalNumber()).isEqualTo("ddc item number");
    assertThat(result.getSubjects()).hasSize(2);
    assertThat(result.getSubjects().getFirst()).isEqualTo("subject person");
    assertThat(result.getSubjects().get(1)).isEqualTo("subject form");
    assertThat(result.getInstances()).hasSize(instancesExpected);
  }

  private void validateInstance(LinkedDataInstanceOnly instanceIndex, Resource instance) {
    assertThat(instanceIndex.getId()).isEqualTo(instance.getId().toString());
    assertTitle(instanceIndex.getTitles().getFirst(), "Primary: mainTitle" + instance.getId(), MAIN);
    assertTitle(instanceIndex.getTitles().get(1), "Primary: subTitle", SUB);
    assertTitle(instanceIndex.getTitles().get(2), "Parallel: mainTitle", MAIN_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(3), "Parallel: subTitle", SUB_PARALLEL);
    assertTitle(instanceIndex.getTitles().get(4), "Variant: mainTitle", MAIN_VARIANT);
    assertTitle(instanceIndex.getTitles().get(5), "Variant: subTitle", SUB_VARIANT);
    assertThat(instanceIndex.getIdentifiers()).hasSize(5);
    assertId(instanceIndex.getIdentifiers().getFirst(), "lccn value", LCCN);
    assertId(instanceIndex.getIdentifiers().get(1), "isbn value", ISBN);
    assertId(instanceIndex.getIdentifiers().get(2), "ian value", IAN);
    assertId(instanceIndex.getIdentifiers().get(3), "otherId value", UNKNOWN);
    assertId(instanceIndex.getIdentifiers().get(4), "wrongId", null);
    assertThat(instanceIndex.getContributors()).isEmpty();
    assertThat(instanceIndex.getPublications()).hasSize(1);
    assertThat(instanceIndex.getPublications().getFirst().getDate()).isNull();
    assertThat(instanceIndex.getPublications().getFirst().getName()).isEqualTo("publication name");
    assertThat(instanceIndex.getEditionStatements()).hasSize(1);
    assertThat(instanceIndex.getEditionStatements().getFirst())
      .isEqualTo(instance.getDoc().get(EDITION.getValue()).get(0).asString());
  }

  private void assertTitle(LinkedDataTitle titleInner, String value, LinkedDataTitle.TypeEnum type) {
    assertThat(titleInner.getValue()).isEqualTo(value);
    assertThat(titleInner.getType()).isEqualTo(type);
  }

  private void assertId(LinkedDataIdentifier idInner, String value, TypeEnum type) {
    assertThat(idInner.getValue()).isEqualTo(value);
    assertThat(idInner.getType()).isEqualTo(type);
  }

  private void assertContributor(LinkedDataContributor contributorInner, String value,
                                 LinkedDataContributor.TypeEnum type, boolean isCreator) {
    assertThat(contributorInner.getName()).isEqualTo(value);
    assertThat(contributorInner.getType()).isEqualTo(type);
    assertThat(contributorInner.getIsCreator()).isEqualTo(isCreator);
  }
}
