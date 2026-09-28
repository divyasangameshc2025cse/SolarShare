package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.model.Installation;
import PROJECT.SOLARSHARE.service.InstallationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/installations")
@CrossOrigin(origins = "*")
public class InstallationController {

    private final InstallationService installationService;

    public InstallationController(InstallationService installationService) {
        this.installationService = installationService;
    }

    @GetMapping
    public ResponseEntity<List<Installation>> getAllInstallations() {
        return ResponseEntity.ok(installationService.getAllInstallations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Installation> getInstallationById(@PathVariable Long id) {
        return ResponseEntity.ok(installationService.getInstallationById(id));
    }

    @PostMapping
    public ResponseEntity<Installation> createInstallation(@Valid @RequestBody Installation installation) {
        return new ResponseEntity<>(installationService.createInstallation(installation), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Installation> updateInstallation(@PathVariable Long id, @Valid @RequestBody Installation installation) {
        return ResponseEntity.ok(installationService.updateInstallation(id, installation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstallation(@PathVariable Long id) {
        installationService.deleteInstallation(id);
        return ResponseEntity.noContent().build();
    }
}
