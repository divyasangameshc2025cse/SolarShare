package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GenerationLogService {

    private final GenerationLogRepository generationLogRepository;
    private final InstallationRepository installationRepository;

    public GenerationLogService(GenerationLogRepository generationLogRepository, InstallationRepository installationRepository) {
        this.generationLogRepository = generationLogRepository;
        this.installationRepository = installationRepository;
    }

    public List<GenerationLog> getAllGenerationLogs() {
        return generationLogRepository.findAll();
    }

    public GenerationLog getGenerationLogById(Long id) {
        return generationLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generation log not found with id: " + id));
    }

    public GenerationLog createGenerationLog(GenerationLog generationLog) {
        if (generationLog.getDate() == null) {
            throw new BadRequestException("Generation date is required");
        }
        if (generationLog.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Generation date cannot be in the future (today: " + LocalDate.now() + ")");
        }
        if (generationLog.getInstallation() == null || generationLog.getInstallation().getId() == null) {
            throw new BadRequestException("Installation ID is required for generation log");
        }
        Installation installation = installationRepository.findById(generationLog.getInstallation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + generationLog.getInstallation().getId()));
        generationLog.setInstallation(installation);
        return generationLogRepository.save(generationLog);
    }

    public GenerationLog updateGenerationLog(Long id, GenerationLog updatedLog) {
        GenerationLog existing = getGenerationLogById(id);

        if (updatedLog.getDate() != null) {
            if (updatedLog.getDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Generation date cannot be in the future (today: " + LocalDate.now() + ")");
            }
            existing.setDate(updatedLog.getDate());
        }

        existing.setGeneratedUnits(updatedLog.getGeneratedUnits());

        if (updatedLog.getInstallation() != null && updatedLog.getInstallation().getId() != null) {
            Installation installation = installationRepository.findById(updatedLog.getInstallation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + updatedLog.getInstallation().getId()));
            existing.setInstallation(installation);
        }

        return generationLogRepository.save(existing);
    }

    public void deleteGenerationLog(Long id) {
        GenerationLog existing = getGenerationLogById(id);
        generationLogRepository.delete(existing);
    }
}
