package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_usage_application")
@Data
@Builder
public class UsageApplication {
    @EmbeddedId
    private UsageApplicationId usageApplicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("applicationId")
    @JsonIgnore
    private Application application;

    @Column(name = "can_manage", nullable = false)
    private boolean canManage;
    @Column(name = "can_use", nullable = false)
    private boolean canUse;
}
