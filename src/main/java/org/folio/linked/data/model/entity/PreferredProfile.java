package org.folio.linked.data.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.folio.linked.data.model.entity.pk.PreferredProfilePk;

@Data
@Entity
@Table(name = "preferred_profiles")
@Accessors(chain = true)
public class PreferredProfile {

  @EmbeddedId
  private PreferredProfilePk id;

  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private Profile profile;
}
