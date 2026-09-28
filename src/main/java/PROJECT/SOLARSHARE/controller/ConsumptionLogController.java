package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.model.ConsumptionLog;
import PROJECT.SOLARSHARE.service.ConsumptionLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumption")
@CrossOrigin(origins = "*")
public class ConsumptionLogController {

    private final ConsumptionLogService consumptionLogService;

    public ConsumptionLogController(ConsumptionLogService consumptionLogService) {
        this.consumptionLogService = consumptionLogService;
    }

    @GetMapping
    public ResponseEntity<List<ConsumptionLog>> getAllConsumptionLogs() {
        return ResponseEntity.ok(consumptionLogService.getAllConsumptionLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsumptionLog> getConsumptionLogById(@PathVariable Long id) {
        return ResponseEntity.ok(consumptionLogService.getConsumptionLogById(id));
    }

    @PostMapping
    public ResponseEntity<ConsumptionLog> createConsumptionLog(@Valid @RequestBody ConsumptionLog consumptionLog) {
        return new ResponseEntity<>(consumptionLogService.createConsumptionLog(consumptionLog), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsumptionLog> updateConsumptionLog(@PathVariable Long id, @Valid @RequestBody ConsumptionLog consumptionLog) {
        return ResponseEntity.ok(consumptionLogService.updateConsumptionLog(id, consumptionLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsumptionLog(@PathVariable Long id) {
        consumptionLogService.deleteConsumptionLog(id);
        return ResponseEntity.noContent().build();
    }
}
