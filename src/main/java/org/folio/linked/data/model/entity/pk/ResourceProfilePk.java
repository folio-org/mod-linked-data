package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ResourceProfilePk implements Serializable {
  private Long resourceHash;
  private Integer profileId;
}
