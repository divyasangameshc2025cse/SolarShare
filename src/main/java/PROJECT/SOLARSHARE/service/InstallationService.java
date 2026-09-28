package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallationService {

    private final InstallationRepository installationRepository;
    private final HouseholdRepository householdRepository;
    private final GenerationLogRepository generationLogRepository;

    public InstallationService(InstallationRepository installationRepository,
                               HouseholdRepository householdRepository,
                               GenerationLogRepository generationLogRepository) {
        this.installationRepository = installationRepository;
        this.householdRepository = householdRepository;
        this.generationLogRepository = generationLogRepository;
    }

    public List<Installation> getAllInstallations() {
        return installationRepository.findAll();
    }

    public Installation getInstallationById(Long id) {
        return installationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + id));
    }

    public Installation createInstallation(Installation installation) {
        if (installation.getCapacityKw() == null || installation.getCapacityKw() <= 0) {
            throw new BadRequestException("Installation capacity must be greater than 0 kW");
        }
        return installationRepository.save(installation);
    }

    public Installation updateInstallation(Long id, Installation updatedInstallation) {
        Installation existing = getInstallationById(id);

        if (updatedInstallation.getCapacityKw() == null || updatedInstallation.getCapacityKw() <= 0) {
            throw new BadRequestException("Installation capacity must be greater than 0 kW");
        }

        // Check that new capacity is not less than total allocated capacity of existing households
        List<Household> households = householdRepository.findByInstallationId(id);
        double totalAllocated = households.stream().mapToDouble(Household::getAllocatedKwh).sum();
        if (updatedInstallation.getCapacityKw() < totalAllocated) {
            throw new BadRequestException("Cannot reduce installation capacity to " + updatedInstallation.getCapacityKw() + " kW because registered households are already allocated " + Math.round(totalAllocated * 100.0) / 100.0 + " kWh total.");
        }

        existing.setName(updatedInstallation.getName());
        existing.setLocation(updatedInstallation.getLocation());
        existing.setCapacityKw(updatedInstallation.getCapacityKw());
        return installationRepository.save(existing);
    }

    public void deleteInstallation(Long id) {
        Installation existing = getInstallationById(id);

        List<Household> households = householdRepository.findByInstallationId(id);
        if (!households.isEmpty()) {
            throw new BadRequestException("Cannot delete installation '" + existing.getName() + "' because it has " + households.size() + " registered household(s). Please delete or reassign them first.");
        }

        List<GenerationLog> genLogs = generationLogRepository.findByInstallationId(id);
        if (!genLogs.isEmpty()) {
            throw new BadRequestException("Cannot delete installation '" + existing.getName() + "' because it has " + genLogs.size() + " generation log(s). Please delete them first.");
        }

        installationRepository.delete(existing);
    }
}
