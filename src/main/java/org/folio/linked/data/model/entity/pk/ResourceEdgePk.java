package org.folio.linked.data.model.entity.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Embeddable
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class ResourceEdgePk implements Serializable {

  @Column(name = "source_hash")
  private Long sourceHash;

  @Column(name = "target_hash")
  private Long targetHash;

  @Column(name = "predicate_hash")
  private Long predicateHash;

}
