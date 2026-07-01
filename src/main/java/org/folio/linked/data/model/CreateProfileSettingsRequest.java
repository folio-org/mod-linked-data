package org.folio.linked.data.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.folio.linked.data.domain.dto.CustomProfileSettingsRequestDto;
import org.folio.linked.data.validation.ProfileSettingsNameUniqueConstraint;

@ProfileSettingsNameUniqueConstraint
@AllArgsConstructor
@Data
public class CreateProfileSettingsRequest {
  @NotNull
  private Integer profileId;

  @NotNull
  @Valid
  private CustomProfileSettingsRequestDto customProfileSettingsRequestDto;
}
