package PROJECT.SOLARSHARE.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Household name is required")
    private String householdName;

    @NotNull(message = "Allocated kWh is required")
    @Positive(message = "Allocated kWh must be positive")
    private Double allocatedKwh;

    @ManyToOne
    @JoinColumn(name = "installation_id", nullable = false)
    @NotNull(message = "Installation is required")
    private Installation installation;

    public Household() {
    }

    public Household(Long id, String householdName, Double allocatedKwh, Installation installation) {
        this.id = id;
        this.householdName = householdName;
        this.allocatedKwh = allocatedKwh;
        this.installation = installation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public void setHouseholdName(String householdName) {
        this.householdName = householdName;
    }

    public Double getAllocatedKwh() {
        return allocatedKwh;
    }

    public void setAllocatedKwh(Double allocatedKwh) {
        this.allocatedKwh = allocatedKwh;
    }

    public Installation getInstallation() {
        return installation;
    }

    public void setInstallation(Installation installation) {
        this.installation = installation;
    }
}
