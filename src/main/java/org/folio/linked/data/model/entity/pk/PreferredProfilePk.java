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
public class PreferredProfilePk implements Serializable {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "resource_type", nullable = false)
  private Long resourceType;
}
