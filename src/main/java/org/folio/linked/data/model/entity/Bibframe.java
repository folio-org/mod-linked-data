package org.folio.linked.data.model.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Bibframe {

  private UUID id;

  private boolean toBeFilled;

}
