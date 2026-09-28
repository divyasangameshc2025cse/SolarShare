package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import org.springframework.stereotype.Service;

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

        // Interpret ratio: if > 1.0 (e.g. 40), treat as percentage (0.40)
        double ratio = household.getAllocationRatio();
        double effectiveRatio = ratio > 1.0 ? ratio / 100.0 : ratio;

        // Calculate allocated share
        double allocatedShare = generationLog.getGeneratedUnits() * effectiveRatio;

        // RULE 1: Check that total allocated solar units for the generation day does not exceed total generated units
        List<ConsumptionLog> dayLogs = consumptionLogRepository.findByGenerationLogId(generationLog.getId());
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
