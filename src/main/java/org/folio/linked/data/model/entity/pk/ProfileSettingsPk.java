package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSettingsPk implements Serializable {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "profile_id", nullable = false)
  private Integer profileId;
}
