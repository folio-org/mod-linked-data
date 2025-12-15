package org.folio.linked.data.validation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionSystemException;

@IntegrationTest
class ResourceTypeValidatorIT {
  @Autowired
  private ResourceRepository resourceRepository;

  @Test
  void shouldNotPersistResourceWithoutTypes() {
    // given
    var resourceWithoutTypes = new Resource().setIdAndRefreshEdges(999L);

    // then
    var thrown = assertThrows(TransactionSystemException.class,
      () -> resourceRepository.save(resourceWithoutTypes));

    assertThat(thrown.getCause()).isInstanceOf(RollbackException.class);
    assertThat(thrown.getCause().getCause()).isInstanceOf(ConstraintViolationException.class);
    var cve = (ConstraintViolationException) thrown.getCause().getCause();
    assertThat(cve.getConstraintViolations()).hasSize(1);
    assertThat(cve.getConstraintViolations().iterator().next().getMessage())
      .isEqualTo("required_resource_type");
  }
}
