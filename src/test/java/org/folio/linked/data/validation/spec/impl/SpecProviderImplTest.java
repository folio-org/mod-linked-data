package org.folio.linked.data.validation.spec.impl;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.List;
import org.folio.linked.data.integration.client.SpecClient;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.rspec.domain.dto.SpecificationRuleDtoCollection;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SpecProviderImplTest {

  @Mock
  private SpecClient client;

  @InjectMocks
  private SpecProviderImpl specProvider;

  @Test
  void shouldReturn_specRules_whenSpecificationStorageIsAvailable() {
    //given
    var specRuleId = randomUUID();
    var specifications = new SpecificationDtoCollection();
    var specRule = new SpecificationRuleDto();
    var specRules = new SpecificationRuleDtoCollection();
    specRules.setRules(List.of(specRule));
    specifications.setSpecifications(List.of(new SpecificationDto().id(specRuleId)));
    doReturn(ResponseEntity.ok().body(specifications)).when(client).getBibMarcSpecs();
    doReturn(ResponseEntity.ok().body(specRules)).when(client).getSpecRules(specRuleId);

    //expect
    assertEquals(List.of(specRule), specProvider.getSpecRules());
  }

  @Test
  void shouldReturn_emptyList_whenSpecificationStorageIsNotAvailable() {
    //given
    when(client.getBibMarcSpecs()).thenThrow(FeignException.class);

    //expect
    assertEquals(emptyList(), specProvider.getSpecRules());
  }
}
