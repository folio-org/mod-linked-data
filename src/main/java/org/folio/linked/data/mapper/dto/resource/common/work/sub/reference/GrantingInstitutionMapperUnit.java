package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GRANTING_INSTITUTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import java.util.Set;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationResponse;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ORGANIZATION, predicate = GRANTING_INSTITUTION, requestDto = Reference.class)
public class GrantingInstitutionMapperUnit extends ReferenceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    Dissertation.class,
    DissertationResponse.class
  );

  public GrantingInstitutionMapperUnit(ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super((grantingInstitution, destination) -> {
      if (destination instanceof DissertationResponse dissertation) {
        dissertation.addGrantingInstitutionReferenceItem(grantingInstitution);
      }
    }, resourceMarcAuthorityService);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
