package org.folio.linked.data.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.folio.linked.data.model.entity.pk.PreferredProfileSettingsPk;

@Data
@Entity
@Table(name = "preferred_profile_settings")
@Accessors(chain = true)
public class PreferredProfileSettings {

  @EmbeddedId
  private PreferredProfileSettingsPk id;

  @MapsId("profileId")
  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private Profile profile;

  @ManyToOne
  @JoinColumn(name = "profile_settings_id", nullable = false)
  private ProfileSettings profileSettings;
}
