package PROJECT.SOLARSHARE.controller;

import PROJECT.SOLARSHARE.model.Household;
import PROJECT.SOLARSHARE.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households")
@CrossOrigin(origins = "*")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping
    public ResponseEntity<List<Household>> getAllHouseholds() {
        return ResponseEntity.ok(householdService.getAllHouseholds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Household> getHouseholdById(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.getHouseholdById(id));
    }

    @PostMapping
    public ResponseEntity<Household> createHousehold(@Valid @RequestBody Household household) {
        return new ResponseEntity<>(householdService.createHousehold(household), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Household> updateHousehold(@PathVariable Long id, @Valid @RequestBody Household household) {
        return ResponseEntity.ok(householdService.updateHousehold(id, household));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHousehold(@PathVariable Long id) {
        householdService.deleteHousehold(id);
        return ResponseEntity.noContent().build();
    }
}
