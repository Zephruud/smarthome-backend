package smarthomebackend.smarthomebackend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import smarthomebackend.smarthomebackend.model.DeviceType;

@Data
public class CreateDeviceRequest {
    @NotBlank
    private String name;
    @NotNull
    private DeviceType type;
}
