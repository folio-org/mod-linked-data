package org.folio.linked.data.model.entity.imprt;

import static jakarta.persistence.GenerationType.SEQUENCE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class ImportEventFailedResource {
  private static final String IMPORT_EVENT_FAILED_RESOURCE_SEQ_GEN = "import_event_failed_resource_seq";

  @Id
  @Column(nullable = false)
  @SequenceGenerator(name = IMPORT_EVENT_FAILED_RESOURCE_SEQ_GEN, allocationSize = 1)
  @GeneratedValue(strategy = SEQUENCE, generator = IMPORT_EVENT_FAILED_RESOURCE_SEQ_GEN)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "import_event_result_id", nullable = false)
  private ImportEventResult importEventResult;

  private String rawResource;

  private String reason;
}
