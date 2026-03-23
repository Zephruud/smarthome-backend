package smarthomebackend.smarthomebackend.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "SmartHome backend is running";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
