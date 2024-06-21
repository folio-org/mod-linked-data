package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GRANTING_INSTITUTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import java.util.Set;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ORGANIZATION, predicate = GRANTING_INSTITUTION, dtoClass = Reference.class)
public class GrantingInstitutionMapperUnit extends ReferenceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(Dissertation.class);

  public GrantingInstitutionMapperUnit(ResourceRepository resourceRepository) {
    super((grantingInstitution, destination) -> {
      if (destination instanceof Dissertation dissertation) {
        dissertation.addGrantingInstitutionReferenceItem(grantingInstitution);
      }
    }, resourceRepository);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
