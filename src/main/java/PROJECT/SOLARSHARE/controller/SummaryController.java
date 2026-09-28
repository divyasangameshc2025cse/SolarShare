package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/summary")
@CrossOrigin(origins = "*")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/{householdId}")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @PathVariable Long householdId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(summaryService.getMonthlySummary(householdId, month, year));
    }
}
