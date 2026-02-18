package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISBN;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.TransactionSystemException;

@IntegrationTest
class PrimaryTitleEntityValidatorIT extends ITBase {

  @Autowired
  private ResourceRepository resourceRepository;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void saveNotInstanceAndWorkResourceWithNoPrimaryMainTitle_shouldBeOk() {
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
  void saveInstanceResourceWithNoPrimaryMainTitle_shouldFail() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(456L)
      .addTypes(INSTANCE);

    // when
    var thrown = assertThrows(TransactionSystemException.class,
      () -> resourceRepository.save(resource));

    // then
    assertThat(thrown.getCause()).isInstanceOf(RollbackException.class);
    assertThat(thrown.getCause().getCause()).isInstanceOf(ConstraintViolationException.class);
    var cve = (ConstraintViolationException) thrown.getCause().getCause();
    assertThat(cve.getConstraintViolations()).hasSize(1);
    assertThat(cve.getConstraintViolations().iterator().next().getMessage())
      .isEqualTo("required_primary_main_title");
  }

  @Test
  void saveWorkResourceWithNoPrimaryMainTitle_shouldFail() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(789L)
      .addTypes(WORK, BOOKS);

    // when
    var thrown = assertThrows(TransactionSystemException.class,
      () -> resourceRepository.save(resource));

    // then
    assertThat(thrown.getCause()).isInstanceOf(RollbackException.class);
    assertThat(thrown.getCause().getCause()).isInstanceOf(ConstraintViolationException.class);
    var cve = (ConstraintViolationException) thrown.getCause().getCause();
    assertThat(cve.getConstraintViolations()).hasSize(1);
    assertThat(cve.getConstraintViolations().iterator().next().getMessage())
      .isEqualTo("required_primary_main_title");
  }
}
