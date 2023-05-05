package org.folio.linked.data.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "bibframe")
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(exclude = {"id"})
public class Bibframe {

  private UUID id;

  private boolean toBeFilled;

}
