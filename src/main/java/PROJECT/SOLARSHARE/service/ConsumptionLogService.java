package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsumptionLogService {

    private final ConsumptionLogRepository consumptionLogRepository;
    private final HouseholdRepository householdRepository;
    private final GenerationLogRepository generationLogRepository;

    public ConsumptionLogService(ConsumptionLogRepository consumptionLogRepository,
                                 HouseholdRepository householdRepository,
                                 GenerationLogRepository generationLogRepository) {
        this.consumptionLogRepository = consumptionLogRepository;
        this.householdRepository = householdRepository;
        this.generationLogRepository = generationLogRepository;
    }

    public List<ConsumptionLog> getAllConsumptionLogs() {
        return consumptionLogRepository.findAll();
    }

    public ConsumptionLog getConsumptionLogById(Long id) {
        return consumptionLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consumption log not found with id: " + id));
    }

    public ConsumptionLog createConsumptionLog(ConsumptionLog log) {
        if (log.getHousehold() == null || log.getHousehold().getId() == null) {
            throw new BadRequestException("Household ID is required");
        }
        if (log.getGenerationLog() == null || log.getGenerationLog().getId() == null) {
            throw new BadRequestException("Generation log ID is required");
        }

        Household household = householdRepository.findById(log.getHousehold().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id: " + log.getHousehold().getId()));

        GenerationLog generationLog = generationLogRepository.findById(log.getGenerationLog().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Generation log not found with id: " + log.getGenerationLog().getId()));

        log.setHousehold(household);
        log.setGenerationLog(generationLog);

        calculateSharesAndValidate(log, null);

        return consumptionLogRepository.save(log);
    }

    public ConsumptionLog updateConsumptionLog(Long id, ConsumptionLog updatedLog) {
        ConsumptionLog existing = getConsumptionLogById(id);

        if (updatedLog.getHousehold() != null && updatedLog.getHousehold().getId() != null) {
            Household household = householdRepository.findById(updatedLog.getHousehold().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Household not found with id: " + updatedLog.getHousehold().getId()));
            existing.setHousehold(household);
        }

        if (updatedLog.getGenerationLog() != null && updatedLog.getGenerationLog().getId() != null) {
            GenerationLog generationLog = generationLogRepository.findById(updatedLog.getGenerationLog().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Generation log not found with id: " + updatedLog.getGenerationLog().getId()));
            existing.setGenerationLog(generationLog);
        }

        existing.setDate(updatedLog.getDate());
        existing.setUnitsConsumed(updatedLog.getUnitsConsumed());

        calculateSharesAndValidate(existing, existing.getId());

        return consumptionLogRepository.save(existing);
    }

    public void deleteConsumptionLog(Long id) {
        ConsumptionLog existing = getConsumptionLogById(id);
        consumptionLogRepository.delete(existing);
    }

    private void calculateSharesAndValidate(ConsumptionLog log, Long currentLogId) {
        Household household = log.getHousehold();
        GenerationLog generationLog = log.getGenerationLog();

        if (log.getDate() == null) {
            throw new BadRequestException("Consumption date is required");
        }
        if (log.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Consumption date cannot be in the future (today: " + LocalDate.now() + ")");
        }

        // Enforce that consumption date must match the generation log date
        if (generationLog.getDate() != null && !generationLog.getDate().equals(log.getDate())) {
            throw new BadRequestException("Consumption date (" + log.getDate() + ") must match the Generation Log date (" + generationLog.getDate() + ")");
        }

        if (log.getUnitsConsumed() == null || log.getUnitsConsumed() < 0) {
            throw new BadRequestException("Units consumed must be zero or positive");
        }

        // Check that consumption units do not exceed generated units
        if (log.getUnitsConsumed() > generationLog.getGeneratedUnits()) {
            throw new BadRequestException("Consumption units (" + log.getUnitsConsumed() + " kWh) cannot exceed generated units (" + generationLog.getGeneratedUnits() + " kWh)");
        }

        // Enforce that household and generation log belong to the same installation
        if (household.getInstallation() != null && generationLog.getInstallation() != null &&
            !household.getInstallation().getId().equals(generationLog.getInstallation().getId())) {
            throw new BadRequestException("Household '" + household.getHouseholdName() + "' belongs to installation '" +
                household.getInstallation().getName() + "', but selected generation log is from '" +
                generationLog.getInstallation().getName() + "'");
        }

        Installation installation = household.getInstallation() != null ? household.getInstallation() : generationLog.getInstallation();
        double installationCapacity = (installation != null && installation.getCapacityKw() != null && installation.getCapacityKw() > 0)
                ? installation.getCapacityKw() : 1.0;

        // Calculate allocated share from household's allocated kWh portion of installation capacity
        double allocatedShare = generationLog.getGeneratedUnits() * (household.getAllocatedKwh() / installationCapacity);

        List<ConsumptionLog> dayLogs = consumptionLogRepository.findByGenerationLogId(generationLog.getId());

        // Prevent duplicate consumption log for the same household on the same generation log
        boolean alreadyLogged = dayLogs.stream()
                .anyMatch(l -> (currentLogId == null || !l.getId().equals(currentLogId)) &&
                               l.getHousehold() != null && l.getHousehold().getId().equals(household.getId()));
        if (alreadyLogged) {
            throw new BadRequestException("Household '" + household.getHouseholdName() + "' has already recorded consumption for this generation log (Date: " + generationLog.getDate() + ")");
        }

        // RULE 1: Check that total allocated solar units for the generation day does not exceed total generated units
        double existingAllocatedSum = dayLogs.stream()
                .filter(l -> currentLogId == null || !l.getId().equals(currentLogId))
                .mapToDouble(ConsumptionLog::getAllocatedShare)
                .sum();

        if (existingAllocatedSum + allocatedShare > generationLog.getGeneratedUnits() + 0.0001) {
            throw new BadRequestException("Total allocated units cannot exceed generated units");
        }

        // RULE 2: Household exported units = max(allocatedShare - unitsConsumed, 0)
        double exportedUnits = Math.max(allocatedShare - log.getUnitsConsumed(), 0.0);

        log.setAllocatedShare(Math.round(allocatedShare * 100.0) / 100.0);
        log.setExportedUnits(Math.round(exportedUnits * 100.0) / 100.0);
    }
}
