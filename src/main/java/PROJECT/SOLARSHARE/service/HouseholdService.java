package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final InstallationRepository installationRepository;

    public HouseholdService(HouseholdRepository householdRepository, InstallationRepository installationRepository) {
        this.householdRepository = householdRepository;
        this.installationRepository = installationRepository;
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
        return householdRepository.save(household);
    }

    public Household updateHousehold(Long id, Household updatedHousehold) {
        Household existing = getHouseholdById(id);
        existing.setHouseholdName(updatedHousehold.getHouseholdName());
        existing.setAllocationRatio(updatedHousehold.getAllocationRatio());

        if (updatedHousehold.getInstallation() != null && updatedHousehold.getInstallation().getId() != null) {
            Installation installation = installationRepository.findById(updatedHousehold.getInstallation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + updatedHousehold.getInstallation().getId()));
            existing.setInstallation(installation);
        }

        return householdRepository.save(existing);
    }

    public void deleteHousehold(Long id) {
        Household existing = getHouseholdById(id);
        householdRepository.delete(existing);
    }
}
