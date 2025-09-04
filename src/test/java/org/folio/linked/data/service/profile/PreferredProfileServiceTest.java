package org.folio.linked.data.service.profile;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.model.entity.PreferredProfile;
import org.folio.linked.data.model.entity.Profile;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;
import org.folio.linked.data.repo.PreferredProfileRepository;
import org.folio.linked.data.repo.ProfileRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class PreferredProfileServiceTest {
  @InjectMocks
  private PreferredProfileServiceImpl service;
  @Mock
  private ProfileRepository profileRepository;
  @Mock
  private FolioExecutionContext executionContext;
  @Mock
  private PreferredProfileRepository preferredProfileRepository;
  @Mock
  private ResourceTypeRepository typeRepository;

  @Test
  void setPreferredProfile_shouldUpdateExistedRecord() {
    // given
    var newPrefferedProfile = new Profile().setId(2);
    doReturn(Optional.of(newPrefferedProfile)).when(profileRepository).findById(newPrefferedProfile.getId());
    var resourceTypeEntity = new ResourceTypeEntity(INSTANCE.getHash(), INSTANCE.getUri(), null);
    doReturn(resourceTypeEntity).when(typeRepository).findByUri(resourceTypeEntity.getUri());
    var userId = UUID.randomUUID();
    doReturn(userId).when(executionContext).getUserId();
    var ppId = new PreferredProfilePk(userId, INSTANCE.getHash());
    var existedRecord = new PreferredProfile()
      .setId(ppId)
      .setProfile(new Profile().setId(1));
    doReturn(Optional.of(existedRecord)).when(preferredProfileRepository).findById(ppId);

    // when
    service.setPreferredProfile(newPrefferedProfile.getId(), resourceTypeEntity.getUri());

    // then
    verify(preferredProfileRepository).findById(ppId);
    verify(preferredProfileRepository).save(existedRecord.setProfile(newPrefferedProfile));
  }

}
