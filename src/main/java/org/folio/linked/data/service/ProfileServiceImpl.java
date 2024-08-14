package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.PROFILE_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.repo.ProfileRepository;
import org.springframework.stereotype.Service;

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
