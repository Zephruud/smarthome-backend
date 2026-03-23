package smarthomebackend.smarthomebackend.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import smarthomebackend.smarthomebackend.dto.CreateDeviceRequest;
import smarthomebackend.smarthomebackend.dto.CreateRoomRequest;
import smarthomebackend.smarthomebackend.dto.DeviceToggleRequest;
import smarthomebackend.smarthomebackend.model.Device;
import smarthomebackend.smarthomebackend.model.Room;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

import java.util.List;

@CrossOrigin(origins = "*") // for testing – make stricter later
@RestController
@RequestMapping("/api")
public class RoomController {

    private final SmartHomeService service;

    public RoomController(SmartHomeService service) {
        this.service = service;
    }

    @GetMapping("/rooms")
    public List<Room> listRooms() { return service.getRooms(); }

    @GetMapping("/rooms/{roomId}")
    public Room getRoom(@PathVariable String roomId) { return service.getRoom(roomId); }

    @PostMapping("/rooms")
    public Room createRoom(@Valid @RequestBody CreateRoomRequest req) {
        return service.createRoom(req.getName());
    }

    @PostMapping("/rooms/{roomId}/devices")
    public Device addDevice(@PathVariable String roomId,
                            @Valid @RequestBody CreateDeviceRequest req) {
        return service.addDevice(roomId, req.getName(), req.getType());
    }

    @PatchMapping("/rooms/{roomId}/devices/{deviceId}")
    public Device toggleDevice(@PathVariable String roomId,
                               @PathVariable String deviceId,
                               @Valid @RequestBody DeviceToggleRequest req) {
        return service.toggleDevice(roomId, deviceId, req.getOn());
    }
    @DeleteMapping("/rooms/{roomId}/devices/{deviceId}")
    public void deleteDevice(@PathVariable String roomId,
                             @PathVariable String deviceId) {
        service.deleteDevice(roomId, deviceId);
    }

}
