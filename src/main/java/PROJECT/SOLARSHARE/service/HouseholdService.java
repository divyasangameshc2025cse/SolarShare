package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final InstallationRepository installationRepository;
    private final ConsumptionLogRepository consumptionLogRepository;

    public HouseholdService(HouseholdRepository householdRepository,
                            InstallationRepository installationRepository,
                            ConsumptionLogRepository consumptionLogRepository) {
        this.householdRepository = householdRepository;
        this.installationRepository = installationRepository;
        this.consumptionLogRepository = consumptionLogRepository;
    }

    public List<Household> getAllHouseholds() {
        return householdRepository.findAll();
    }

    public Household getHouseholdById(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id: " + id));
    }

    public Household createHousehold(Household household) {
        if (household.getInstallation() == null || household.getInstallation().getId() == null) {
            throw new BadRequestException("Installation ID is required for household");
        }
        Installation installation = installationRepository.findById(household.getInstallation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + household.getInstallation().getId()));
        household.setInstallation(installation);

        validateAllocatedKwh(household, null, installation);

        return householdRepository.save(household);
    }

    public Household updateHousehold(Long id, Household updatedHousehold) {
        Household existing = getHouseholdById(id);
        existing.setHouseholdName(updatedHousehold.getHouseholdName());

        Installation installation = existing.getInstallation();
        if (updatedHousehold.getInstallation() != null && updatedHousehold.getInstallation().getId() != null) {
            installation = installationRepository.findById(updatedHousehold.getInstallation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + updatedHousehold.getInstallation().getId()));
            existing.setInstallation(installation);
        }

        existing.setAllocatedKwh(updatedHousehold.getAllocatedKwh());
        validateAllocatedKwh(existing, existing.getId(), installation);

        return householdRepository.save(existing);
    }

    public void deleteHousehold(Long id) {
        Household existing = getHouseholdById(id);

        List<PROJECT.SOLARSHARE.model.ConsumptionLog> logs = consumptionLogRepository.findByHouseholdId(id);
        if (!logs.isEmpty()) {
            throw new BadRequestException("Cannot delete household '" + existing.getHouseholdName() + "' because it has " + logs.size() + " consumption log(s). Please delete them first.");
        }

        householdRepository.delete(existing);
    }

    private void validateAllocatedKwh(Household household, Long currentHouseholdId, Installation installation) {
        if (household.getAllocatedKwh() == null || household.getAllocatedKwh() <= 0) {
            throw new BadRequestException("Allocated kWh must be greater than 0");
        }

        double requestedKwh = household.getAllocatedKwh();
        double installationCapacity = installation.getCapacityKw();

        if (requestedKwh > installationCapacity) {
            throw new BadRequestException("Allocated kWh (" + requestedKwh + " kWh) cannot exceed Installation capacity (" + installationCapacity + " kW)");
        }

        // Validate total allocated kWh for the installation does not exceed installation capacity
        List<Household> existingHouseholds = householdRepository.findByInstallationId(installation.getId());
        double existingSum = existingHouseholds.stream()
                .filter(h -> currentHouseholdId == null || !h.getId().equals(currentHouseholdId))
                .mapToDouble(Household::getAllocatedKwh)
                .sum();

        if (existingSum + requestedKwh > installationCapacity + 0.0001) {
            double totalAttempted = Math.round((existingSum + requestedKwh) * 100.0) / 100.0;
            double currentTotal = Math.round(existingSum * 100.0) / 100.0;
            throw new BadRequestException("Total allocated capacity (" + totalAttempted + " kWh) cannot exceed Installation capacity (" + installationCapacity + " kW). Currently allocated: " + currentTotal + " kWh");
        }

        household.setAllocatedKwh(Math.round(requestedKwh * 100.0) / 100.0);
    }
}
