package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.ConsumptionLogRepository;
import PROJECT.SOLARSHARE.repository.GenerationLogRepository;
import PROJECT.SOLARSHARE.repository.InstallationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GenerationLogService {

    private final GenerationLogRepository generationLogRepository;
    private final InstallationRepository installationRepository;
    private final ConsumptionLogRepository consumptionLogRepository;

    public GenerationLogService(GenerationLogRepository generationLogRepository,
                                  InstallationRepository installationRepository,
                                  ConsumptionLogRepository consumptionLogRepository) {
        this.generationLogRepository = generationLogRepository;
        this.installationRepository = installationRepository;
        this.consumptionLogRepository = consumptionLogRepository;
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

        // Prevent duplicate generation log for same installation and date
        Optional<GenerationLog> existingDuplicate = generationLogRepository.findByInstallationIdAndDate(installation.getId(), generationLog.getDate());
        if (existingDuplicate.isPresent()) {
            throw new BadRequestException("A generation log already exists for installation '" + installation.getName() + "' on date " + generationLog.getDate());
        }

        generationLog.setInstallation(installation);
        return generationLogRepository.save(generationLog);
    }

    public GenerationLog updateGenerationLog(Long id, GenerationLog updatedLog) {
        GenerationLog existing = getGenerationLogById(id);

        LocalDate targetDate = updatedLog.getDate() != null ? updatedLog.getDate() : existing.getDate();
        if (targetDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Generation date cannot be in the future (today: " + LocalDate.now() + ")");
        }

        Installation targetInstallation = existing.getInstallation();
        if (updatedLog.getInstallation() != null && updatedLog.getInstallation().getId() != null) {
            targetInstallation = installationRepository.findById(updatedLog.getInstallation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Installation not found with id: " + updatedLog.getInstallation().getId()));
        }

        // Prevent duplicate generation log for same installation and date
        Optional<GenerationLog> duplicate = generationLogRepository.findByInstallationIdAndDate(targetInstallation.getId(), targetDate);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new BadRequestException("A generation log already exists for installation '" + targetInstallation.getName() + "' on date " + targetDate);
        }

        existing.setDate(targetDate);
        existing.setGeneratedUnits(updatedLog.getGeneratedUnits());
        existing.setInstallation(targetInstallation);

        return generationLogRepository.save(existing);
    }

    public void deleteGenerationLog(Long id) {
        GenerationLog existing = getGenerationLogById(id);

        List<PROJECT.SOLARSHARE.model.ConsumptionLog> linkedLogs = consumptionLogRepository.findByGenerationLogId(id);
        if (!linkedLogs.isEmpty()) {
            throw new BadRequestException("Cannot delete Generation Log #" + id + " because it has " + linkedLogs.size() + " consumption log(s) linked to it. Please delete them first.");
        }

        generationLogRepository.delete(existing);
    }
}
