package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import PROJECT.SOLARSHARE.service.GenerationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GenerationLogServiceTest {

    private GenerationLogRepository generationLogRepository;
    private InstallationRepository installationRepository;
    private ConsumptionLogRepository consumptionLogRepository;
    private GenerationLogService generationLogService;

    private Installation installation;

    @BeforeEach
    void setUp() {
        generationLogRepository = Mockito.mock(GenerationLogRepository.class);
        installationRepository = Mockito.mock(InstallationRepository.class);
        consumptionLogRepository = Mockito.mock(ConsumptionLogRepository.class);

        generationLogService = new GenerationLogService(
                generationLogRepository,
                installationRepository,
                consumptionLogRepository
        );

        installation = new Installation(1L, "Green Valley", "Building A", 50.0);
        when(installationRepository.findById(1L)).thenReturn(Optional.of(installation));
    }

    @Test
    void testCreateGenerationLog_Success() {
        GenerationLog gen = new GenerationLog(null, LocalDate.of(2026, 9, 28), 100.0, installation);
        when(generationLogRepository.findByInstallationIdAndDate(1L, LocalDate.of(2026, 9, 28))).thenReturn(Optional.empty());
        when(generationLogRepository.save(any(GenerationLog.class))).thenAnswer(i -> i.getArgument(0));

        GenerationLog saved = generationLogService.createGenerationLog(gen);
        assertEquals(100.0, saved.getGeneratedUnits());
        assertEquals(installation, saved.getInstallation());
    }

    @Test
    void testCreateGenerationLog_DuplicateDate_ThrowsBadRequestException() {
        GenerationLog existing = new GenerationLog(1L, LocalDate.of(2026, 9, 28), 80.0, installation);
        when(generationLogRepository.findByInstallationIdAndDate(1L, LocalDate.of(2026, 9, 28))).thenReturn(Optional.of(existing));

        GenerationLog newLog = new GenerationLog(null, LocalDate.of(2026, 9, 28), 100.0, installation);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> generationLogService.createGenerationLog(newLog));
        assertTrue(ex.getMessage().contains("already exists for installation"));
    }

    @Test
    void testCreateGenerationLog_FutureDate_ThrowsBadRequestException() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        GenerationLog gen = new GenerationLog(null, futureDate, 100.0, installation);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> generationLogService.createGenerationLog(gen));
        assertTrue(ex.getMessage().contains("cannot be in the future"));
    }

    @Test
    void testDeleteGenerationLog_Success() {
        GenerationLog existing = new GenerationLog(1L, LocalDate.of(2026, 9, 28), 100.0, installation);
        when(generationLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(Collections.emptyList());

        generationLogService.deleteGenerationLog(1L);
        verify(generationLogRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteGenerationLog_WithConsumptionLogs_ThrowsBadRequestException() {
        GenerationLog existing = new GenerationLog(1L, LocalDate.of(2026, 9, 28), 100.0, installation);
        ConsumptionLog log = new ConsumptionLog();

        when(generationLogRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(consumptionLogRepository.findByGenerationLogId(1L)).thenReturn(List.of(log));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> generationLogService.deleteGenerationLog(1L));
        assertTrue(ex.getMessage().contains("consumption log(s) linked to it"));
    }
}
