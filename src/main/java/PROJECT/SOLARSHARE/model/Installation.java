package PROJECT.SOLARSHARE.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "installations")
public class Installation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Installation name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity in kW is required")
    @Positive(message = "Capacity must be a positive number")
    private Double capacityKw;

    public Installation() {
    }

    public Installation(Long id, String name, String location, Double capacityKw) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.capacityKw = capacityKw;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getCapacityKw() {
        return capacityKw;
    }

    public void setCapacityKw(Double capacityKw) {
        this.capacityKw = capacityKw;
    }
}
