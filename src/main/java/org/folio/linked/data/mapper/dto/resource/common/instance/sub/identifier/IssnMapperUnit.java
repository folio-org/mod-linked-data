package org.folio.linked.data.mapper.dto.resource.common.instance.sub.identifier;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_ISSN;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.IdentifierFieldResponse;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.IdentifierResponse;
import org.folio.linked.data.domain.dto.IssnField;
import org.folio.linked.data.domain.dto.IssnFieldResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ID_ISSN, predicate = MAP, requestDto = IssnField.class)
public class IssnMapperUnit extends AbstractIdentifierMapperUnit {

  protected IssnMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  protected ResourceTypeDictionary getIdentifierType() {
    return ID_ISSN;
  }

  @Override
  protected IdentifierFieldResponse toFieldResponse(IdentifierResponse identifierResponse) {
    return new IssnFieldResponse().issn(identifierResponse);
  }

  @Override
  protected IdentifierRequest toIdentifierRequest(Object dto) {
    return ((IssnField) dto).getIssn();
  }
}
