package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionSystemException;

@IntegrationTest
class PrimaryTitleEntityValidatorIT {

  @Autowired
  private ResourceRepository resourceRepository;
  @MockitoBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void saveNotInstanceAndWorkResourceWithNoPrimaryMainTitle_shouldBeOk() {
    // given
    var resource = new Resource()
      .setId(123L);

    // when
    var result = resourceRepository.save(resource);

    // then
    assertThat(result).isEqualTo(resource);
  }

  @Test
  void saveInstanceResourceWithNoPrimaryMainTitle_shouldFail() {
    // given
    var resource = new Resource()
      .setId(456L)
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
      .setId(789L)
      .addTypes(WORK);

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
