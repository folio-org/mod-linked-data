package org.folio.linked.data.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceProfile {
  @Id
  private Long resourceHash;
  private Integer profileId;
}
