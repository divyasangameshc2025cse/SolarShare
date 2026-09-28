package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import PROJECT.SOLARSHARE.service.InstallationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InstallationServiceTest {

    private InstallationRepository installationRepository;
    private InstallationService installationService;

    @BeforeEach
    void setUp() {
        installationRepository = Mockito.mock(InstallationRepository.class);
        installationService = new InstallationService(installationRepository);
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
        when(installationRepository.save(any(Installation.class))).thenAnswer(i -> i.getArgument(0));

        Installation updated = installationService.updateInstallation(1L, updateInfo);
        assertEquals("New Name", updated.getName());
        assertEquals("Roof 2", updated.getLocation());
        assertEquals(45.0, updated.getCapacityKw());
    }

    @Test
    void testDeleteInstallation() {
        Installation existing = new Installation(1L, "Plant 1", "Roof 1", 30.0);
        when(installationRepository.findById(1L)).thenReturn(Optional.of(existing));

        installationService.deleteInstallation(1L);
        verify(installationRepository, times(1)).delete(existing);
    }
}
