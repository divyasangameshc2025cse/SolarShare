package PROJECT.SOLARSHARE.repository;

import PROJECT.SOLARSHARE.model.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    List<Household> findByInstallationId(Long installationId);
}
