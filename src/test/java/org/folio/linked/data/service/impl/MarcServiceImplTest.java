package org.folio.linked.data.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.client.SrsClient;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcServiceImplTest {

  @InjectMocks
  private MarcServiceImpl marcService;

  @Mock
  private SrsClient srsClient;

  @ParameterizedTest
  @CsvSource({
    "a, a",
    "a, m"
  })
  void isSupportedByInventoryId_shouldReturnTrue(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getSourceStorageRecordFormattedById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertTrue(marcService.isSupportedByInventoryId(inventoryId));
  }

  @ParameterizedTest
  @CsvSource({
    "' ', ' '",
    "' ',  a",
    "' ',  m",
    "a, ' '",
    "a, s",
    "o, a",
    "o, m",
  })
  void isSupportedByInventoryId_shouldReturnFalse(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getSourceStorageRecordFormattedById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertFalse(marcService.isSupportedByInventoryId(inventoryId));
  }

  private Record createRecord(char type, char level) {
    var leader = "04809n   a2200865 i 4500";
    leader = leader.substring(0, 6) + type + level + leader.substring(8);
    var content = Map.of("leader", leader);
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }
}
