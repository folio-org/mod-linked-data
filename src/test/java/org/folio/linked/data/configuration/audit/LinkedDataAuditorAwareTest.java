package org.folio.linked.data.configuration.audit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LinkedDataAuditorAwareTest {

  @InjectMocks
  private LinkedDataAuditorAware linkedDataAuditorAware;

  @Mock
  private FolioExecutionContext folioExecutionContext;

  @Test
  void getCurrentAuditor_shouldReturnUserId() {
    // given
    var userId = UUID.randomUUID();
    when(folioExecutionContext.getUserId()).thenReturn(userId);

    // when
    var optionalAuditor = linkedDataAuditorAware.getCurrentAuditor();

    // then
    assertThat(optionalAuditor)
      .get()
      .satisfies(actualUserId -> assertEquals(userId, actualUserId));
  }
}
