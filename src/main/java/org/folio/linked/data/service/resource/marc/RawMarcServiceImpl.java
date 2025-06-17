package org.folio.linked.data.service.resource.marc;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.RawMarcRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RawMarcServiceImpl implements  RawMarcService {
  private final RawMarcRepository rawMarcRepository;

  @Override
  public Optional<String> getRawMarc(Resource resource) {
    if (resource.isNotOfType(INSTANCE)) {
      return Optional.empty();
    }
    return getRawMarc(resource.getId());
  }

  @Override
  public Optional<String> getRawMarc(Long resourceId) {
    return rawMarcRepository.findById(resourceId).map(RawMarc::getContent);
  }

  @Override
  public void saveRawMarc(Resource resource, String rawMarcContent) {
    if (resource.isNotOfType(INSTANCE)) {
      return;
    }
    ofNullable(rawMarcContent)
      .map(marc -> new RawMarc(resource).setContent(marc))
      .ifPresent(rawMarcRepository::save);
  }
}
