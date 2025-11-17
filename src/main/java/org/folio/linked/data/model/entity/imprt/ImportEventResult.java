package org.folio.linked.data.model.entity.imprt;

import static jakarta.persistence.CascadeType.ALL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "eventTs")
@Accessors(chain = true)
public class ImportEventResult {

  @Id
  @Column(nullable = false)
  private Long eventTs;
  @Column(nullable = false)
  private Long jobId;
  @Column(nullable = false)
  private Timestamp startDate;
  @Column(nullable = false)
  private Timestamp endDate;
  @Column(nullable = false)
  private Integer resourcesCount;
  @Column(nullable = false)
  private Integer createdCount;
  @Column(nullable = false)
  private Integer updatedCount;
  @Column(nullable = false)
  private Integer failedCount;
  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "importEventResult", cascade = ALL, orphanRemoval = true)
  private Set<ImportEventFailedResource> failedResources = new LinkedHashSet<>();

}
