package org.folio.linked.data.mapper.dto.resource.common.instance.sub.identifier;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LccnFieldResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_LCCN, predicate = MAP, requestDto = LccnField.class)
public class LccnMapperUnit extends AbstractIdentifierMapperUnit {

  protected LccnMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_LCCN;
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new LccnFieldResponse().lccn(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((LccnField) dto).getLccn();
  }
}
