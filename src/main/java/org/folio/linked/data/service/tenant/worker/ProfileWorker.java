package org.folio.linked.data.service.tenant.worker;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.ProfileService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Order(3)
@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ProfileWorker implements TenantServiceWorker {

  private static final String PROFILE_DIRECTORY = "profiles";
  private final ObjectMapper objectMapper;
  private final ProfileService profileService;

  @Override
  public void afterTenantUpdate(String tenantId, TenantAttributes tenantAttributes) {
    log.info("Saving profiles for tenant: {}", tenantId);

    var profilesDirectory = getClass().getClassLoader().getResource(PROFILE_DIRECTORY);
    try (var files = Files.list(Paths.get(profilesDirectory.toURI()))) {
      files
        .filter(Files::isRegularFile)
        .forEach(this::saveProfile);
    } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
      log.error("Failed to read profiles from directory: {}", PROFILE_DIRECTORY, e);
    }
  }

  private void saveProfile(Path file) {
    try (var inputStream = Files.newInputStream(file)) {
      var profile = objectMapper.readValue(inputStream, Profile.class);
      var profileStr = objectMapper.writeValueAsString(profile.value());
      profileService.saveProfile(profile.id(), profile.name(), profile.resourceType(), profileStr);
    } catch (IOException e) {
      log.error("Failed to process file: {}", file, e);
    }
  }

  record Profile(Integer id, String name, String resourceType, JsonNode value) {}
}
