package PROJECT.SOLARSHARE.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Entity
@Table(name = "consumption_logs")
public class ConsumptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Units consumed is required")
    @PositiveOrZero(message = "Units consumed must be zero or positive")
    private Double unitsConsumed;

    private Double allocatedShare;

    private Double exportedUnits;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    @NotNull(message = "Household is required")
    private Household household;

    @ManyToOne
    @JoinColumn(name = "generation_log_id", nullable = false)
    @NotNull(message = "Generation log is required")
    private GenerationLog generationLog;

    public ConsumptionLog() {
    }

    public ConsumptionLog(Long id, LocalDate date, Double unitsConsumed, Double allocatedShare, Double exportedUnits, Household household, GenerationLog generationLog) {
        this.id = id;
        this.date = date;
        this.unitsConsumed = unitsConsumed;
        this.allocatedShare = allocatedShare;
        this.exportedUnits = exportedUnits;
        this.household = household;
        this.generationLog = generationLog;
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

    public Double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(Double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    public Double getAllocatedShare() {
        return allocatedShare;
    }

    public void setAllocatedShare(Double allocatedShare) {
        this.allocatedShare = allocatedShare;
    }

    public Double getExportedUnits() {
        return exportedUnits;
    }

    public void setExportedUnits(Double exportedUnits) {
        this.exportedUnits = exportedUnits;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public GenerationLog getGenerationLog() {
        return generationLog;
    }

    public void setGenerationLog(GenerationLog generationLog) {
        this.generationLog = generationLog;
    }
}
