package org.folio.linked.data.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@Table(name = "resource_profile")
@Accessors(chain = true)
@NoArgsConstructor
public class ResourceProfile {

  @Id
  private Long resourceHash;

  private Integer profileId;

  public ResourceProfile(Long resourceHash, Integer profileId) {
    this.resourceHash = resourceHash;
    this.profileId = profileId;
  }
}
