package PROJECT.SOLARSHARE.repository;

import PROJECT.SOLARSHARE.model.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, Long> {
    List<ConsumptionLog> findByGenerationLogId(Long generationLogId);
    List<ConsumptionLog> findByHouseholdId(Long householdId);
}
