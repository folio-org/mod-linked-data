package org.folio.linked.data.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkedDataApplicationServiceTest {

  @Mock
  private JdbcTemplate jdbcTemplate;

  @InjectMocks
  private LinkedDataApplicationService linkedDataApplicationService;

  @Test
  void shouldReturnTrueIfModuleIsInstalled() {
    // given
    doReturn(true).when(jdbcTemplate).query((String) any(), (ResultSetExtractor<Object>) any());

    // when
    var result = linkedDataApplicationService.isModuleInstalled("tenant-with-linked-data-module");

    // then
    assertTrue(result);
  }

  @Test
  void shouldReturnFalseIfModuleIsNotInstalled() {
    // given
    doReturn(false).when(jdbcTemplate).query((String) any(), (ResultSetExtractor<Object>) any());

    // when
    var result = linkedDataApplicationService.isModuleInstalled("tenant-without-linked-data-module");

    // then
    assertFalse(result);
  }
}
