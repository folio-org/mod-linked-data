package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.TransactionSystemException;

@IntegrationTest
class AuthorityNameEntityValidatorIT extends ITBase {

  @Autowired
  private ResourceRepository resourceRepository;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void saveNonAuthorityResourceWithNoName_shouldBeOk() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(123L)
      .addTypes(ID_ISBN);

    // when
    var result = resourceRepository.save(resource);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void saveAuthorityResourceWithNoName_shouldFail() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(456L)
      .addTypes(PERSON);

    // when
    var thrown = assertThrows(TransactionSystemException.class,
      () -> resourceRepository.save(resource));

    // then
    assertThat(thrown.getCause()).isInstanceOf(RollbackException.class);
    assertThat(thrown.getCause().getCause()).isInstanceOf(ConstraintViolationException.class);
    var cve = (ConstraintViolationException) thrown.getCause().getCause();
    assertThat(cve.getConstraintViolations())
      .extracting(ConstraintViolation::getMessage)
      .contains("required_authority_name");
  }

  @Test
  void saveAuthorityResourceWithName_shouldBeOk() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(789L)
      .addTypes(PERSON)
      .setDoc(getJsonNode(Map.of(NAME.getValue(), List.of("Authority Name"))));

    // when
    var result = resourceRepository.save(resource);

    // then
    assertThat(result).isEqualTo(resource);
  }
}
