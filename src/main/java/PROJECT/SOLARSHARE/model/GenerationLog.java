package PROJECT.SOLARSHARE.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Entity
@Table(name = "generation_logs")
public class GenerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Generated units are required")
    @PositiveOrZero(message = "Generated units must be zero or positive")
    private Double generatedUnits;

    @ManyToOne
    @JoinColumn(name = "installation_id", nullable = false)
    @NotNull(message = "Installation is required")
    private Installation installation;

    public GenerationLog() {
    }

    public GenerationLog(Long id, LocalDate date, Double generatedUnits, Installation installation) {
        this.id = id;
        this.date = date;
        this.generatedUnits = generatedUnits;
        this.installation = installation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getGeneratedUnits() {
        return generatedUnits;
    }

    public void setGeneratedUnits(Double generatedUnits) {
        this.generatedUnits = generatedUnits;
    }

    public Installation getInstallation() {
        return installation;
    }

    public void setInstallation(Installation installation) {
        this.installation = installation;
    }
}
