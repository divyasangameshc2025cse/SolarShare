package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstallationService {

    private final InstallationRepository installationRepository;

    public InstallationService(InstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
    }

    public List<Installation> getAllInstallations() {
        return installationRepository.findAll();
    }

    public Installation getInstallationById(Long id) {
        return installationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + id));
    }

    public Installation createInstallation(Installation installation) {
        return installationRepository.save(installation);
    }

    public Installation updateInstallation(Long id, Installation updatedInstallation) {
        Installation existing = getInstallationById(id);
        existing.setName(updatedInstallation.getName());
        existing.setLocation(updatedInstallation.getLocation());
        existing.setCapacityKw(updatedInstallation.getCapacityKw());
        return installationRepository.save(existing);
    }

    public void deleteInstallation(Long id) {
        Installation existing = getInstallationById(id);
        installationRepository.delete(existing);
    }
}
