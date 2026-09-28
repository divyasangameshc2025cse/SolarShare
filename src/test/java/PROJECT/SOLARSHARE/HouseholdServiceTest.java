package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import PROJECT.SOLARSHARE.service.HouseholdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HouseholdServiceTest {

    private HouseholdRepository householdRepository;
    private InstallationRepository installationRepository;
    private HouseholdService householdService;

    private Installation installation;

    @BeforeEach
    void setUp() {
        householdRepository = Mockito.mock(HouseholdRepository.class);
        installationRepository = Mockito.mock(InstallationRepository.class);
        householdService = new HouseholdService(householdRepository, installationRepository);

        installation = new Installation(1L, "Green Valley", "Building A", 50.0);
        when(installationRepository.findById(1L)).thenReturn(Optional.of(installation));
    }

    @Test
    void testCreateHousehold_ValidAllocatedKwh() {
        Household h = new Household(null, "Flat 101", 20.0, installation);
        when(householdRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());
        when(householdRepository.save(any(Household.class))).thenAnswer(i -> i.getArgument(0));

        Household created = householdService.createHousehold(h);
        assertEquals(20.0, created.getAllocatedKwh());
    }

    @Test
    void testCreateHousehold_AllocatedKwhExceedsInstallationCapacity_ThrowsBadRequestException() {
        // Installation capacity is 50.0 kW, requested is 60.0 kWh
        Household h = new Household(null, "Flat 101", 60.0, installation);
        when(householdRepository.findByInstallationId(1L)).thenReturn(Collections.emptyList());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> householdService.createHousehold(h));
        assertTrue(ex.getMessage().contains("cannot exceed Installation capacity"));
    }

    @Test
    void testCreateHousehold_TotalInstallationKwhExceedsCapacity_ThrowsBadRequestException() {
        // Existing: House 1 (30 kWh), House 2 (15 kWh) -> Total 45 kWh out of 50 kW
        Household h1 = new Household(1L, "Flat 101", 30.0, installation);
        Household h2 = new Household(2L, "Flat 102", 15.0, installation);
        when(householdRepository.findByInstallationId(1L)).thenReturn(List.of(h1, h2));

        // Attempting to add House 3 with 10 kWh -> 45 + 10 = 55 kWh > 50 kW
        Household h3 = new Household(null, "Flat 103", 10.0, installation);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> householdService.createHousehold(h3));
        assertTrue(ex.getMessage().contains("Total allocated capacity"));
    }

    @Test
    void testCreateHousehold_ZeroOrNegativeAllocatedKwh_ThrowsBadRequestException() {
        Household hZero = new Household(null, "Flat 101", 0.0, installation);
        assertThrows(BadRequestException.class, () -> householdService.createHousehold(hZero));

        Household hNeg = new Household(null, "Flat 101", -5.0, installation);
        assertThrows(BadRequestException.class, () -> householdService.createHousehold(hNeg));
    }

    @Test
    void testUpdateHousehold_ValidAllocatedKwh() {
        Household existing = new Household(1L, "Flat 101", 20.0, installation);
        when(householdRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(List.of(existing));
        when(householdRepository.save(any(Household.class))).thenAnswer(i -> i.getArgument(0));

        Household update = new Household(null, "Flat 101 - Updated", 25.0, installation);
        Household result = householdService.updateHousehold(1L, update);

        assertEquals(25.0, result.getAllocatedKwh());
        assertEquals("Flat 101 - Updated", result.getHouseholdName());
    }

    @Test
    void testUpdateHousehold_ExceedsCapacity_ThrowsBadRequestException() {
        Household existing = new Household(1L, "Flat 101", 20.0, installation);
        Household other = new Household(2L, "Flat 102", 25.0, installation);
        when(householdRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(householdRepository.findByInstallationId(1L)).thenReturn(List.of(existing, other));

        // Attempting to increase House 1 to 30.0 kWh (30 + 25 = 55 > 50 kW capacity)
        Household update = new Household(null, "Flat 101 - Updated", 30.0, installation);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> householdService.updateHousehold(1L, update));
        assertTrue(ex.getMessage().contains("Total allocated capacity"));
    }
}
