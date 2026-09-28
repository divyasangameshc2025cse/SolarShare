package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.service.ConsumptionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConsumptionLogServiceTest {

    private ConsumptionLogRepository consumptionLogRepository;
    private HouseholdRepository householdRepository;
    private GenerationLogRepository generationLogRepository;
    private ConsumptionLogService consumptionLogService;

    private Installation installation;
    private Household houseA;
    private Household houseB;
    private Household houseC;
    private GenerationLog genLog;

    @BeforeEach
    void setUp() {
        consumptionLogRepository = Mockito.mock(ConsumptionLogRepository.class);
        householdRepository = Mockito.mock(HouseholdRepository.class);
        generationLogRepository = Mockito.mock(GenerationLogRepository.class);

        consumptionLogService = new ConsumptionLogService(
                consumptionLogRepository,
                householdRepository,
                generationLogRepository
        );

        installation = new Installation(1L, "Green Valley", "Building A", 50.0);
        houseA = new Household(1L, "House A", 20.0, installation); // 20 kWh of 50 kW (40%)
        houseB = new Household(2L, "House B", 15.0, installation); // 15 kWh of 50 kW (30%)
        houseC = new Household(3L, "House C", 15.0, installation); // 15 kWh of 50 kW (30%)

        genLog = new GenerationLog(1L, LocalDate.of(2026, 9, 28), 100.0, installation);

        when(generationLogRepository.findById(1L)).thenReturn(Optional.of(genLog));
        when(householdRepository.findById(1L)).thenReturn(Optional.of(houseA));
        when(householdRepository.findById(2L)).thenReturn(Optional.of(houseB));
        when(householdRepository.findById(3L)).thenReturn(Optional.of(houseC));
    }

    @Test
    void testRule2_ExportedUnitsCalculation_WhenAllocatedGreaterThanConsumed() {
        // Allocated: 100 * 0.40 = 40, Consumed: 25 -> Exported: 15
        ConsumptionLog log = new ConsumptionLog();
        log.setDate(LocalDate.of(2026, 9, 28));
        log.setUnitsConsumed(25.0);
        log.setHousehold(houseA);
        log.setGenerationLog(genLog);

        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(Collections.emptyList());
        when(consumptionLogRepository.save(any(ConsumptionLog.class))).thenAnswer(i -> i.getArgument(0));

        ConsumptionLog saved = consumptionLogService.createConsumptionLog(log);

        assertEquals(40.0, saved.getAllocatedShare());
        assertEquals(15.0, saved.getExportedUnits());
    }

    @Test
    void testRule2_ExportedUnitsCalculation_WhenConsumedGreaterThanAllocated() {
        // Allocated: 100 * 0.40 = 40, Consumed: 50 -> Exported: 0 (Never negative)
        ConsumptionLog log = new ConsumptionLog();
        log.setDate(LocalDate.of(2026, 9, 28));
        log.setUnitsConsumed(50.0);
        log.setHousehold(houseA);
        log.setGenerationLog(genLog);

        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(Collections.emptyList());
        when(consumptionLogRepository.save(any(ConsumptionLog.class))).thenAnswer(i -> i.getArgument(0));

        ConsumptionLog saved = consumptionLogService.createConsumptionLog(log);

        assertEquals(40.0, saved.getAllocatedShare());
        assertEquals(0.0, saved.getExportedUnits());
    }

    @Test
    void testRule1_ValidAllocation_TotalAllocatedEqualsGenerated() {
        // Log 1: House A (40 allocated)
        ConsumptionLog logA = new ConsumptionLog(1L, LocalDate.of(2026, 9, 28), 25.0, 40.0, 15.0, houseA, genLog);
        // Log 2: House B (30 allocated)
        ConsumptionLog logB = new ConsumptionLog(2L, LocalDate.of(2026, 9, 28), 20.0, 30.0, 10.0, houseB, genLog);

        // Now adding House C (30 allocated) -> Total = 40 + 30 + 30 = 100 (equal to 100 generated units)
        ConsumptionLog logC = new ConsumptionLog();
        logC.setDate(LocalDate.of(2026, 9, 28));
        logC.setUnitsConsumed(20.0);
        logC.setHousehold(houseC);
        logC.setGenerationLog(genLog);

        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(List.of(logA, logB));
        when(consumptionLogRepository.save(any(ConsumptionLog.class))).thenAnswer(i -> i.getArgument(0));

        ConsumptionLog saved = consumptionLogService.createConsumptionLog(logC);

        assertEquals(30.0, saved.getAllocatedShare());
        assertEquals(10.0, saved.getExportedUnits());
    }

    @Test
    void testRule1_ExceedingGeneratedUnits_ThrowsBadRequestException() {
        // Log 1: House A (40 allocated)
        ConsumptionLog logA = new ConsumptionLog(1L, LocalDate.of(2026, 9, 28), 25.0, 40.0, 15.0, houseA, genLog);
        // Log 2: House B (40 allocated)
        ConsumptionLog logB = new ConsumptionLog(2L, LocalDate.of(2026, 9, 28), 20.0, 40.0, 20.0, houseB, genLog);

        // Now adding House C (30 allocated) -> Total = 40 + 40 + 30 = 110 > 100 -> must throw
        ConsumptionLog logC = new ConsumptionLog();
        logC.setDate(LocalDate.of(2026, 9, 28));
        logC.setUnitsConsumed(20.0);
        logC.setHousehold(houseC);
        logC.setGenerationLog(genLog);

        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(List.of(logA, logB));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            consumptionLogService.createConsumptionLog(logC);
        });

        assertEquals("Total allocated units cannot exceed generated units", ex.getMessage());
    }

    @Test
    void testUnitsConsumedGreaterThanGeneratedUnits_ThrowsBadRequestException() {
        GenerationLog smallGenLog = new GenerationLog(2L, LocalDate.of(2026, 9, 28), 20.0, installation);
        when(generationLogRepository.findById(2L)).thenReturn(Optional.of(smallGenLog));

        // Consumed 25.0 > Generated 20.0
        ConsumptionLog log = new ConsumptionLog();
        log.setDate(LocalDate.of(2026, 9, 28));
        log.setUnitsConsumed(25.0);
        log.setHousehold(houseA);
        log.setGenerationLog(smallGenLog);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            consumptionLogService.createConsumptionLog(log);
        });

        assertTrue(ex.getMessage().contains("cannot exceed generated units"));
    }

    @Test
    void testConsumptionDateMismatch_ThrowsBadRequestException() {
        // Generation is on Sep 20, but consumption date is Sep 21
        GenerationLog pastGenLog = new GenerationLog(3L, LocalDate.of(2026, 9, 20), 100.0, installation);
        when(generationLogRepository.findById(3L)).thenReturn(Optional.of(pastGenLog));

        ConsumptionLog log = new ConsumptionLog();
        log.setDate(LocalDate.of(2026, 9, 21));
        log.setUnitsConsumed(10.0);
        log.setHousehold(houseA);
        log.setGenerationLog(pastGenLog);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            consumptionLogService.createConsumptionLog(log);
        });

        assertTrue(ex.getMessage().contains("must match the Generation Log date"));
    }

    @Test
    void testDuplicateHouseholdConsumptionLog_ThrowsBadRequestException() {
        ConsumptionLog existing = new ConsumptionLog(1L, LocalDate.of(2026, 9, 28), 20.0, 40.0, 20.0, houseA, genLog);
        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(List.of(existing));

        // Attempting to log again for houseA on same generation log
        ConsumptionLog duplicateLog = new ConsumptionLog();
        duplicateLog.setDate(LocalDate.of(2026, 9, 28));
        duplicateLog.setUnitsConsumed(15.0);
        duplicateLog.setHousehold(houseA);
        duplicateLog.setGenerationLog(genLog);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            consumptionLogService.createConsumptionLog(duplicateLog);
        });
        assertTrue(ex.getMessage().contains("already recorded consumption"));
    }

    @Test
    void testCrossInstallationMismatch_ThrowsBadRequestException() {
        Installation inst2 = new Installation(2L, "Plant 2", "Roof 2", 50.0);
        Household houseOther = new Household(4L, "House Other", 20.0, inst2);
        when(householdRepository.findById(4L)).thenReturn(Optional.of(houseOther));

        ConsumptionLog log = new ConsumptionLog();
        log.setDate(LocalDate.of(2026, 9, 28));
        log.setUnitsConsumed(10.0);
        log.setHousehold(houseOther); // Belongs to inst2
        log.setGenerationLog(genLog); // Belongs to installation 1

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            consumptionLogService.createConsumptionLog(log);
        });
        assertTrue(ex.getMessage().contains("belongs to installation"));
    }
}
