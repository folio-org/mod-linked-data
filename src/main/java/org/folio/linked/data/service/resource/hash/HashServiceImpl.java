package org.folio.linked.data.service.resource.hash;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashServiceImpl implements HashService {

  private final ResourceModelMapper resourceModelMapper;
  private final FingerprintHashService fingerprintHashService;

  @Override
  public Long hash(@NonNull Resource resource) {
    return fingerprintHashService.hash(resourceModelMapper.toModel(resource));
  }

}
