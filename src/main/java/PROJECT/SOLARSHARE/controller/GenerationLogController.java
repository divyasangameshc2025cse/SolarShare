package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.model.GenerationLog;
import PROJECT.SOLARSHARE.service.GenerationLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generation")
@CrossOrigin(origins = "*")
public class GenerationLogController {

    private final GenerationLogService generationLogService;

    public GenerationLogController(GenerationLogService generationLogService) {
        this.generationLogService = generationLogService;
    }

    @GetMapping
    public ResponseEntity<List<GenerationLog>> getAllGenerationLogs() {
        return ResponseEntity.ok(generationLogService.getAllGenerationLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenerationLog> getGenerationLogById(@PathVariable Long id) {
        return ResponseEntity.ok(generationLogService.getGenerationLogById(id));
    }

    @PostMapping
    public ResponseEntity<GenerationLog> createGenerationLog(@Valid @RequestBody GenerationLog generationLog) {
        return new ResponseEntity<>(generationLogService.createGenerationLog(generationLog), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenerationLog> updateGenerationLog(@PathVariable Long id, @Valid @RequestBody GenerationLog generationLog) {
        return ResponseEntity.ok(generationLogService.updateGenerationLog(id, generationLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenerationLog(@PathVariable Long id) {
        generationLogService.deleteGenerationLog(id);
        return ResponseEntity.noContent().build();
    }
}
