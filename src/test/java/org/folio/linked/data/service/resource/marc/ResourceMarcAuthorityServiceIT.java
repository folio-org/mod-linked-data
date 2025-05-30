package org.folio.linked.data.service.resource.marc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.e2e.base.IntegrationTestStandalone;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTestStandalone
class ResourceMarcAuthorityServiceIT {
  @Autowired
  private ResourceMarcAuthorityService resourceMarcAuthorityService;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private FolioMetadataRepository folioMetadataRepository;

  @Test
  @SneakyThrows
  void testAuthorityPreferenceToggle_when_authority_is_reverted_back_to_original_state() {
    var srsId = "src_id_01";

    // save version 1 of the authority
    var v1Resource = new Resource()
      .setId(1L)
      .setDoc(objectMapper.readTree("{}"))
      .setTypes(Set.of(ResourceTypeDictionary.PERSON))
      .setLabel("label_01")
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));

    resourceMarcAuthorityService.saveMarcAuthority(v1Resource);
    validateResourceIdBySrsId(srsId, v1Resource.getId());

    // save version 2 of the authority
    var v2Resource = new Resource()
      .setId(2L)
      .setDoc(v1Resource.getDoc())
      .setTypes(v1Resource.getTypes())
      .setLabel(v1Resource.getLabel())
      .setFolioMetadata(v1Resource.getFolioMetadata());

    resourceMarcAuthorityService.saveMarcAuthority(v2Resource);

    var v1ResourceFromDb = resourceTestService.getResourceById("1", 1);
    var v2ResourceFromDb = resourceTestService.getResourceById("2", 1);
    validateResourceIdBySrsId(srsId, v2Resource.getId());
    assertResourceIsPreferred(v2ResourceFromDb);
    assertResourceIsNotPreferred(v1ResourceFromDb);
    assertReplacedBy(v1ResourceFromDb, v2ResourceFromDb);

    // Revert the authority back to the original (v1) version
    resourceMarcAuthorityService.saveMarcAuthority(v1Resource);

    v1ResourceFromDb = resourceTestService.getResourceById("1", 1);
    v2ResourceFromDb = resourceTestService.getResourceById("2", 1);
    validateResourceIdBySrsId(srsId, v1Resource.getId());
    assertResourceIsPreferred(v1ResourceFromDb);
    assertResourceIsNotPreferred(v2ResourceFromDb);
    assertReplacedBy(v2ResourceFromDb, v1ResourceFromDb);
  }

  private void validateResourceIdBySrsId(String srsId, Long expectedResourceId) {
    var resourceIdOptional = folioMetadataRepository.findIdBySrsId(srsId).map(FolioMetadataRepository.IdOnly::getId);
    assertThat(resourceIdOptional).contains(expectedResourceId);
  }

  private void assertResourceIsPreferred(org.folio.linked.data.model.entity.Resource resource) {
    assertThat(resource.isActive()).isTrue();
    assertThat(isPreferred(resource)).isTrue();
    assertThat(resource.getOutgoingEdges()).isEmpty();
  }

  private void assertResourceIsNotPreferred(org.folio.linked.data.model.entity.Resource resource) {
    assertThat(resource.isActive()).isFalse();
    assertThat(isPreferred(resource)).isFalse();
  }

  private void assertReplacedBy(org.folio.linked.data.model.entity.Resource resource,
                                org.folio.linked.data.model.entity.Resource replacedBy) {
    assertThat(resource.getOutgoingEdges()).hasSize(1);
    assertThat(resource.getOutgoingEdges()).allMatch(
      edge -> edge.getPredicate().getUri().equals(REPLACED_BY.getUri()) && edge.getTarget().equals(replacedBy)
    );
  }
}
