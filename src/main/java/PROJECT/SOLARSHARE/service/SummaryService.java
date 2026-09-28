package PROJECT.SOLARSHARE.service;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class SummaryService {

    private final JdbcTemplate jdbcTemplate;
    private final HouseholdRepository householdRepository;

    public SummaryService(JdbcTemplate jdbcTemplate, HouseholdRepository householdRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.householdRepository = householdRepository;
    }

    public Map<String, Object> getMonthlySummary(Long householdId, int month, int year) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (year > currentYear || (year == currentYear && month > currentMonth)) {
            throw new BadRequestException("Future year/month cannot be selected. Current is " + now.getMonth().name() + " " + currentYear);
        }

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new ResourceNotFoundException("Household not found with id: " + householdId));

        String sql = "SELECT " +
                "COALESCE(SUM(allocated_share), 0) AS total_allocated, " +
                "COALESCE(SUM(units_consumed), 0) AS total_consumed, " +
                "COALESCE(SUM(exported_units), 0) AS total_exported " +
                "FROM consumption_logs " +
                "WHERE household_id = ? AND MONTH(date) = ? AND YEAR(date) = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("householdId", household.getId());
            summary.put("householdName", household.getHouseholdName());
            summary.put("month", month);
            summary.put("year", year);
            summary.put("totalAllocatedUnits", Math.round(rs.getDouble("total_allocated") * 100.0) / 100.0);
            summary.put("totalConsumedUnits", Math.round(rs.getDouble("total_consumed") * 100.0) / 100.0);
            summary.put("totalExportedUnits", Math.round(rs.getDouble("total_exported") * 100.0) / 100.0);
            return summary;
        }, householdId, month, year);
    }
}
