package PROJECT.SOLARSHARE.repository;

import PROJECT.SOLARSHARE.model.GenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerationLogRepository extends JpaRepository<GenerationLog, Long> {
    List<GenerationLog> findByInstallationId(Long installationId);
}
