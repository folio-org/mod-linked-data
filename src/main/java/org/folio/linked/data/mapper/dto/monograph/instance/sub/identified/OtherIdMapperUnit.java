package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_UNKNOWN, predicate = MAP, requestDto = OtherIdField.class)
public class OtherIdMapperUnit extends AbstractIdentifierMapperUnit {

  protected OtherIdMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_UNKNOWN;
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new OtherIdFieldResponse().identifier(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((OtherIdField) dto).getIdentifier();
  }
}
