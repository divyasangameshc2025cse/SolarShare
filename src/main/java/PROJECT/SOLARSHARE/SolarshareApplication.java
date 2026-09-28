package PROJECT.SOLARSHARE;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SolarshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarshareApplication.class, args);
	}

	@Bean
	public CommandLineRunner migrateAndFixHouseholdAllocations(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Step 1: If allocation_ratio exists, migrate it to allocated_kwh based on installation capacity
				try {
					jdbcTemplate.execute(
						"UPDATE households h " +
						"JOIN installations i ON h.installation_id = i.id " +
						"SET h.allocated_kwh = ROUND(CASE WHEN h.allocation_ratio > 1 THEN (h.allocation_ratio / 100.0) * i.capacity_kw ELSE h.allocation_ratio * i.capacity_kw END, 1) " +
						"WHERE (h.allocated_kwh IS NULL OR h.allocated_kwh = 0) " +
						"AND h.allocation_ratio IS NOT NULL AND h.allocation_ratio > 0"
					);
				} catch (Exception ignored) {
					// allocation_ratio column might not exist
				}

				// Step 2: For any remaining households where allocated_kwh is NULL or <= 0, distribute installation capacity evenly
				try {
					List<Map<String, Object>> installations = jdbcTemplate.queryForList("SELECT id, capacity_kw FROM installations");
					for (Map<String, Object> inst : installations) {
						Long instId = ((Number) inst.get("id")).longValue();
						Double capacity = ((Number) inst.get("capacity_kw")).doubleValue();

						List<Map<String, Object>> zeroHouseholds = jdbcTemplate.queryForList(
							"SELECT id FROM households WHERE installation_id = ? AND (allocated_kwh IS NULL OR allocated_kwh <= 0)",
							instId
						);

						if (!zeroHouseholds.isEmpty()) {
							List<Map<String, Object>> validHouseholds = jdbcTemplate.queryForList(
								"SELECT COALESCE(SUM(allocated_kwh), 0) AS total_allocated FROM households WHERE installation_id = ? AND allocated_kwh > 0",
								instId
							);
							double currentAllocated = validHouseholds.isEmpty() ? 0.0 : ((Number) validHouseholds.get(0).get("total_allocated")).doubleValue();
							double remainingCapacity = Math.max(capacity - currentAllocated, 0.0);

							double sharePerHouse;
							if (remainingCapacity > 0) {
								sharePerHouse = Math.round((remainingCapacity / zeroHouseholds.size()) * 10.0) / 10.0;
							} else {
								sharePerHouse = Math.round((capacity / (zeroHouseholds.size() + (currentAllocated > 0 ? 1 : 0))) * 10.0) / 10.0;
							}
							if (sharePerHouse <= 0) {
								sharePerHouse = 10.0;
							}

							for (Map<String, Object> h : zeroHouseholds) {
								Long hId = ((Number) h.get("id")).longValue();
								jdbcTemplate.update("UPDATE households SET allocated_kwh = ? WHERE id = ?", sharePerHouse, hId);
							}
						}
					}
				} catch (Exception ex) {
					System.err.println("Error distributing remaining capacity to households: " + ex.getMessage());
				}

				// Step 3: Clean up legacy allocation_ratio column if it still exists
				try {
					jdbcTemplate.execute("ALTER TABLE households DROP COLUMN allocation_ratio");
				} catch (Exception ignored) {
				}
			} catch (Exception e) {
				System.err.println("Migration error: " + e.getMessage());
			}
		};
	}
}
