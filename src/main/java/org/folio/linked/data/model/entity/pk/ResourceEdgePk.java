package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Embeddable
@NoArgsConstructor
@Accessors(chain = true)
public class ResourceEdgePk implements Serializable {

  private Long sourceHash;
  private Long targetHash;
  private Long predicateHash;

}
