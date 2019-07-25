package fr.icdc.ebad.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

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
