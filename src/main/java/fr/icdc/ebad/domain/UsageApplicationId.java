package fr.icdc.ebad.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageApplicationId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
}
