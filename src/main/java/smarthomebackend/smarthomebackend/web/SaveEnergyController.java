package smarthomebackend.smarthomebackend.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

import java.util.Map;

@RestController
@RequestMapping("/api/modes")
@CrossOrigin(origins = "*")
public class SaveEnergyController {

    private final SmartHomeService service;

    public SaveEnergyController(SmartHomeService service) {
        this.service = service;
    }

    @GetMapping("/save-energy")
    public Map<String, Object> getSaveEnergy() {
        return Map.of("enabled", service.isLivingSaveEnergyMode());
    }

    @PutMapping("/save-energy")
    public ResponseEntity<?> setSaveEnergy(@RequestBody Map<String, Object> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        service.setLivingSaveEnergyMode(enabled);
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }
}
