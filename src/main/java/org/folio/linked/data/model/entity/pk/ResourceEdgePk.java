package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ResourceEdgePk implements Serializable {

  private Long sourceHash;
  private Long targetHash;
  private Long predicateHash;

  public ResourceEdgePk(@NonNull ResourceEdgePk that) {
    this.sourceHash = that.sourceHash;
    this.targetHash = that.targetHash;
    this.predicateHash = that.predicateHash;
  }
}
