package org.folio.linked.data.service.impl;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.service.ProfileService;
import org.springframework.stereotype.Service;

import static org.folio.linked.data.util.Constants.PROFILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

  private final ProfileRepository profileRepository;

  @Override
  public String getProfile() {
    return profileRepository.findById(1)
      .map(Profile::getValue)
      .orElseThrow(() -> new NotFoundException(PROFILE_NOT_FOUND));
  }
}
