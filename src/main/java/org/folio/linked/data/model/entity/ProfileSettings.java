package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.folio.linked.data.model.entity.pk.ProfileSettingsPk;

@Data
@Entity
@Table(name = "profile_settings")
@Accessors(chain = true)
public class ProfileSettings {

  @EmbeddedId
  private ProfileSettingsPk id;

  @MapsId("profileId")
  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private Profile profile;

  @Column(name = "settings", nullable = false)
  private String settings;
}
