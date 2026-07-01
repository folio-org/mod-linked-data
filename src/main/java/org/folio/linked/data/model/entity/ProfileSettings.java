package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(
  name = "profile_settings",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "unique_profile_settings_name",
      columnNames = {"user_id", "profile_id", "name"}
      )
  }
)
@Accessors(chain = true)
public class ProfileSettings {
  @Id
  @GeneratedValue(strategy  = GenerationType.SEQUENCE, generator = "profile_settings_id_seq_gen")
  @SequenceGenerator(
    name = "profile_settings_id_seq_gen",
    sequenceName = "profile_settings_id_seq",
    initialValue = 1,
    allocationSize = 1
  )
  private Integer id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @ManyToOne
  @JoinColumn(name = "profile_id", nullable = false)
  private Profile profile;

  @Column(name = "name", nullable = false)
  private String name;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "settings", columnDefinition = "jsonb", nullable = false)
  private String settings;
}
