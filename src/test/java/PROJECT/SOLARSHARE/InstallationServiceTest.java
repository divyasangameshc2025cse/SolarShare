package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import PROJECT.SOLARSHARE.service.InstallationService;
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

class InstallationServiceTest {

    private InstallationRepository installationRepository;
    private HouseholdRepository householdRepository;
    private GenerationLogRepository generationLogRepository;
    private InstallationService installationService;

    @BeforeEach
    void setUp() {
        installationRepository = Mockito.mock(InstallationRepository.class);
        householdRepository = Mockito.mock(HouseholdRepository.class);
        generationLogRepository = Mockito.mock(GenerationLogRepository.class);

        installationService = new InstallationService(
                installationRepository,
                householdRepository,
                generationLogRepository
        );
    }

    @Test
    void testCreateInstallation() {
        Installation inst = new Installation(null, "Solar Plant A", "Rooftop 1", 50.0);
        Installation saved = new Installation(1L, "Solar Plant A", "Rooftop 1", 50.0);

        when(installationRepository.save(any(Installation.class))).thenReturn(saved);

        Installation result = installationService.createInstallation(inst);
        assertNotNull(result.getId());
        assertEquals("Solar Plant A", result.getName());
    }

    @Test
    void testGetInstallationById_NotFound() {
        when(installationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            installationService.getInstallationById(99L);
        });
    }

    @Test
    void testGetAllInstallations() {
        Installation inst1 = new Installation(1L, "Plant 1", "Roof 1", 30.0);
        Installation inst2 = new Installation(2L, "Plant 2", "Roof 2", 40.0);

        when(installationRepository.findAll()).thenReturn(List.of(inst1, inst2));

        List<Installation> list = installationService.getAllInstallations();
        assertEquals(2, list.size());
    }

    @Test
    void testUpdateInstallation() {
        Installation existing = new Installation(1L, "Old Name", "Roof 1", 30.0);
        Installation updateInfo = new Installation(null, "New Name", "Roof 2", 45.0);

        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());
        when(installationRepository.save(any(Installation.class))).thenAnswer(i -> i.getArgument(0));

        Installation updated = installationService.updateInstallation(1L, updateInfo);
        assertEquals("New Name", updated.getName());
        assertEquals("Roof 2", updated.getLocation());
        assertEquals(45.0, updated.getCapacityKw());
    }

    @Test
    void testUpdateInstallation_ReduceCapacityBelowAllocated_ThrowsBadRequestException() {
        Installation existing = new Installation(1L, "Plant 1", "Roof 1", 50.0);
        Household h1 = new Household(1L, "House A", 30.0, existing);
        Household h2 = new Household(2L, "House B", 15.0, existing); // Total = 45 kWh

        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(List.of(h1, h2));

        // Attempting to reduce to 40 kW < 45 kWh allocated
        Installation updateInfo = new Installation(null, "Plant 1", "Roof 1", 40.0);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            installationService.updateInstallation(1L, updateInfo);
        });
        assertTrue(ex.getMessage().contains("Cannot reduce installation capacity"));
    }

    @Test
    void testDeleteInstallation_Success() {
        Installation existing = new Installation(1L, "Plant 1", "Roof 1", 30.0);
        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());
        when(generationLogRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());

        installationService.deleteInstallation(1L);
        verify(installationRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteInstallation_WithRegisteredHouseholds_ThrowsBadRequestException() {
        Installation existing = new Installation(1L, "Plant 1", "Roof 1", 30.0);
        Household h1 = new Household(1L, "House A", 15.0, existing);

        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(List.of(h1));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            installationService.deleteInstallation(1L);
        });
        assertTrue(ex.getMessage().contains("registered household(s)"));
    }

    @Test
    void testDeleteInstallation_WithGenerationLogs_ThrowsBadRequestException() {
        Installation existing = new Installation(1L, "Plant 1", "Roof 1", 30.0);
        GenerationLog g1 = new GenerationLog(1L, LocalDate.of(2026, 9, 28), 100.0, existing);

        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());
        when(generationLogRepository.findByInstallationId(1L)).thenReturn(List.of(g1));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            installationService.deleteInstallation(1L);
        });
        assertTrue(ex.getMessage().contains("generation log(s)"));
    }
}
