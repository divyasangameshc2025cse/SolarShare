package PROJECT.SOLARSHARE;

import PROJECT.SOLARSHARE.exception.BadRequestException;
import PROJECT.SOLARSHARE.exception.ResourceNotFoundException;
import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.repository.HouseholdRepository;
import PROJECT.SOLARSHARE.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SummaryServiceTest {

    private JdbcTemplate jdbcTemplate;
    private HouseholdRepository householdRepository;
    private SummaryService summaryService;

    private Household household;

    @BeforeEach
    void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        householdRepository = Mockito.mock(HouseholdRepository.class);
        summaryService = new SummaryService(jdbcTemplate, householdRepository);

        Installation inst = new Installation(1L, "Green Valley", "Building A", 50.0);
        household = new Household(1L, "Flat 101", 20.0, inst);

        when(householdRepository.findById(1L)).thenReturn(Optional.of(household));
    }

    @Test
    void testGetMonthlySummary_FutureYear_ThrowsBadRequestException() {
        int futureYear = LocalDate.now().getYear() + 1;
        assertThrows(BadRequestException.class, () -> summaryService.getMonthlySummary(1L, 1, futureYear));
    }

    @Test
    void testGetMonthlySummary_FutureMonthInCurrentYear_ThrowsBadRequestException() {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() < 12) {
            int futureMonth = now.getMonthValue() + 1;
            assertThrows(BadRequestException.class, () -> summaryService.getMonthlySummary(1L, futureMonth, now.getYear()));
        }
    }

    @Test
    void testGetMonthlySummary_InvalidMonth_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> summaryService.getMonthlySummary(1L, 13, 2026));
        assertThrows(BadRequestException.class, () -> summaryService.getMonthlySummary(1L, 0, 2026));
    }

    @Test
    void testGetMonthlySummary_ValidMonthAndYear() {
        LocalDate now = LocalDate.now();
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("householdId", 1L);
        mockResult.put("householdName", "Flat 101");
        mockResult.put("month", now.getMonthValue());
        mockResult.put("year", now.getYear());
        mockResult.put("totalAllocatedUnits", 50.0);
        mockResult.put("totalConsumedUnits", 30.0);
        mockResult.put("totalExportedUnits", 20.0);

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(now.getMonthValue()), eq(now.getYear())))
                .thenReturn(mockResult);

        Map<String, Object> summary = summaryService.getMonthlySummary(1L, now.getMonthValue(), now.getYear());
        assertNotNull(summary);
        assertEquals(50.0, summary.get("totalAllocatedUnits"));
        assertEquals(30.0, summary.get("totalConsumedUnits"));
    }
}
