package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MOCKED_RESOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.TransactionSystemException;

@IntegrationTest
class ResourceTypeValidatorIT extends ITBase {
  private static final ResourceTypeEntity MOCKED_TYPE = new ResourceTypeEntity()
    .setHash(ResourceTypeDictionary.MOCKED_RESOURCE.getHash())
    .setUri(ResourceTypeDictionary.MOCKED_RESOURCE.getUri());

  @Autowired
  private ResourceRepository resourceRepository;
  @Autowired
  private ResourceTypeRepository resourceTypeRepository;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @ParameterizedTest(name = "[{index}] {2}")
  @MethodSource("provideInvalidResources")
  void shouldNotPersistInvalidResource(Resource resource, String expectedError, String testDescription) {
    // given
    if (resource.isOfType(MOCKED_RESOURCE)) {
      resourceTypeRepository.save(MOCKED_TYPE);
    }

    // when
    var thrown = assertThrows(TransactionSystemException.class,
      () -> resourceRepository.save(resource));

    // then
    assertThat(thrown.getCause()).isInstanceOf(RollbackException.class);
    assertThat(thrown.getCause().getCause()).isInstanceOf(ConstraintViolationException.class);
    var cve = (ConstraintViolationException) thrown.getCause().getCause();
    assertThat(cve.getConstraintViolations()).hasSize(1);
    assertThat(cve.getConstraintViolations().iterator().next().getMessage()).isEqualTo(expectedError);

    // cleanup
    if (resource.isOfType(MOCKED_RESOURCE)) {
      resourceTypeRepository.delete(MOCKED_TYPE);
    }
  }

  @ParameterizedTest(name = "[{index}] {1}")
  @MethodSource("provideValidResources")
  void shouldPersistValidResource(Resource resource, String testDescription) {
    // when
    var savedResource = resourceRepository.save(resource);

    // then
    assertThat(savedResource).isNotNull();
    assertThat(savedResource.getId()).isEqualTo(resource.getId());
  }

  private static Stream<Arguments> provideInvalidResources() {
    return Stream.of(
      Arguments.of(
        new Resource().setIdAndRefreshEdges(999L),
        "wrong_resource_type",
        "Resource without any types should not be persisted"
      ),
      Arguments.of(
        createResourceWithTitle(998L, MOCKED_RESOURCE),
        "wrong_resource_type",
        "Resource with MOCKED_RESOURCE type should not be persisted"
      ),
      Arguments.of(
        createResourceWithTitle(997L, WORK),
        "wrong_resource_type",
        "Resource with only WORK type should not be persisted"
      )
    );
  }

  private static Stream<Arguments> provideValidResources() {
    return Stream.of(
      Arguments.of(
        createResourceWithTitle(996L, WORK, BOOKS),
        "Resource with WORK and another type should be persisted"
      ),
      Arguments.of(
        createResourceWithTitle(995L, INSTANCE),
        "Resource with single non-WORK type should be persisted"
      )
    );
  }

  private static Resource createResourceWithTitle(Long id, ResourceTypeDictionary... types) {
    var title = createTitleResource(id + 1);
    var resource = new Resource()
      .setIdAndRefreshEdges(id)
      .addTypes(types)
      .setLabel("Test Resource");
    resource.addOutgoingEdge(new ResourceEdge(resource, title, TITLE));
    return resource;
  }

  private static Resource createTitleResource(Long id) {
    var titleDoc = String.format("{\"%s\": [\"Test Title\"]}", MAIN_TITLE.getValue());
    return new Resource()
      .setIdAndRefreshEdges(id)
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(TEST_JSON_MAPPER.readTree(titleDoc))
      .setLabel("Test Title");
  }
}
