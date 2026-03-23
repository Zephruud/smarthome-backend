package smarthomebackend.smarthomebackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceToggleRequest {
    @NotNull
    private Boolean on; // true/false; for WINDOW this means open/closed
}
