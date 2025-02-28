package org.folio.linked.data.service;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private static final int ID = 2;
  private final ProfileRepository profileRepository;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @Override
  public String getProfile() {
    return profileRepository.findById(ID)
      .map(Profile::getValue)
      .orElseThrow(() -> exceptionBuilder.notFoundLdResourceByIdException("Profile", String.valueOf(ID)));
  }
}
