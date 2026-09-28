package PROJECT.SOLARSHARE.repository;

import PROJECT.SOLARSHARE.model.Installation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallationRepository extends JpaRepository<Installation, Long> {
}
