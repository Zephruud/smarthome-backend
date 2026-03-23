package smarthomebackend.smarthomebackend.repo;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import smarthomebackend.smarthomebackend.model.Device;
import smarthomebackend.smarthomebackend.model.DeviceType;
import smarthomebackend.smarthomebackend.model.Room;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SmartHomeRepository {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // ✅ ONE Save Energy flag (global for Living + Bedroom)
    private volatile boolean saveEnergyMode = false;

    public boolean isSaveEnergyMode() { return saveEnergyMode; }
    public void setSaveEnergyMode(boolean enabled) { this.saveEnergyMode = enabled; }

    // keep compatibility with your controller/service naming:
    public boolean isLivingSaveEnergyMode() { return saveEnergyMode; }
    public void setLivingSaveEnergyMode(boolean enabled) { this.saveEnergyMode = enabled; }

    @PostConstruct
    public void seed() {
        // Living
        Room living = new Room(uuid(), "Living Room", new ArrayList<>());
        living.getDevices().add(new Device(uuid(), "Main Light", DeviceType.LIGHT, false));
        living.getDevices().add(new Device(uuid(), "Heater", DeviceType.HEATER, false));
        living.getDevices().add(new Device(uuid(), "Smart Door Lock", DeviceType.DOOR_LOCK, false));

        living.getDevices().add(new Device(uuid(), "Air Temperature Sensor", DeviceType.TEMPERATURE, true));
        living.getDevices().stream()
                .filter(d -> d.getType() == DeviceType.TEMPERATURE)
                .findFirst()
                .ifPresent(t -> t.setValue(22.0));

        living.getDevices().add(new Device(uuid(), "Window", DeviceType.WINDOW, false));

        // Kitchen
        Room kitchen = new Room(uuid(), "Kitchen", new ArrayList<>());
        kitchen.getDevices().add(new Device(uuid(), "Ceiling Light", DeviceType.LIGHT, false));
        kitchen.getDevices().add(new Device(uuid(), "Oven Heater", DeviceType.HEATER, false));
        kitchen.getDevices().add(new Device(uuid(), "Window", DeviceType.WINDOW, false));

        // ✅ Bedroom (heater + temp sensor too)
        Room bedroom = new Room(uuid(), "Bedroom", new ArrayList<>());
        bedroom.getDevices().add(new Device(uuid(), "Night Lamp", DeviceType.LIGHT, false));
        bedroom.getDevices().add(new Device(uuid(), "Heater", DeviceType.HEATER, false));
        bedroom.getDevices().add(new Device(uuid(), "Air Temperature Sensor", DeviceType.TEMPERATURE, true));
        bedroom.getDevices().stream()
                .filter(d -> d.getType() == DeviceType.TEMPERATURE)
                .findFirst()
                .ifPresent(t -> t.setValue(22.0));
        bedroom.getDevices().add(new Device(uuid(), "Window", DeviceType.WINDOW, false));

        // Keller
        Room keller = new Room(uuid(), "Keller", new ArrayList<>());
        keller.getDevices().add(new Device(uuid(), "Cellar Light", DeviceType.LIGHT, false));

// ✅ Dehumidifier (keep as HEATER if you want, but OTHER is cleaner; we keep HEATER because your code uses it)
        keller.getDevices().add(new Device(uuid(), "Dehumidifier", DeviceType.HEATER, false));

// ✅ Window
        keller.getDevices().add(new Device(uuid(), "Window", DeviceType.WINDOW, false));

// ✅ Humidity sensor (initial value e.g. 55%)
        keller.getDevices().add(new Device(uuid(), "Air Humidity Sensor", DeviceType.HUMIDITY, true));
        keller.getDevices().stream()
                .filter(d -> d.getType() == DeviceType.HUMIDITY)
                .findFirst()
                .ifPresent(h -> h.setValue(55.0));

        // Store
        Room store = new Room(uuid(), "Store Room", new ArrayList<>());
        store.getDevices().add(new Device(uuid(), "Storage Light", DeviceType.LIGHT, false));
        store.getDevices().add(new Device(uuid(), "Window", DeviceType.WINDOW, false));

        for (Room r : List.of(living, kitchen, bedroom, keller, store)) {
            rooms.put(r.getId(), r);
        }
    }

    private String uuid() { return UUID.randomUUID().toString(); }

    public List<Room> findAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Optional<Room> findRoomById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Room createRoom(String name) {
        Room r = new Room(uuid(), name, new ArrayList<>());
        rooms.put(r.getId(), r);
        return r;
    }
    public Room getRoom(String roomId) {
        return findRoomById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }
    public Optional<Device> findDevice(String roomId, String deviceId) {
        return findRoomById(roomId)
                .flatMap(room -> room.getDevices().stream()
                        .filter(d -> d.getId().equals(deviceId))
                        .findFirst());
    }

    public Device addDevice(String roomId, String name, DeviceType type) {
        Room room = rooms.get(roomId);
        if (room == null) throw new NoSuchElementException("Room not found");
        Device d = new Device(uuid(), name, type, false);
        room.getDevices().add(d);
        return d;
    }

    public boolean deleteDevice(String roomId, String deviceId) {
        Room room = rooms.get(roomId);
        if (room == null) return false;
        return room.getDevices().removeIf(d -> d.getId().equals(deviceId));
    }

    // Motion lights helper
    public void setFirstLightInRoomByName(String roomName, boolean on) {
        for (Room r : rooms.values()) {
            if (r.getName() != null && r.getName().equalsIgnoreCase(roomName)) {
                r.getDevices().stream()
                        .filter(d -> d.getType() == DeviceType.LIGHT)
                        .findFirst()
                        .ifPresent(light -> light.setOn(on));
            }
        }
    }

    // Room lookup
    public Optional<Room> findRoomByName(String roomName) {
        return rooms.values().stream()
                .filter(r -> r.getName() != null && r.getName().equalsIgnoreCase(roomName))
                .findFirst();
    }

    // Temperature store
    public void setTemperatureValue(String roomName, double tempC) {
        findRoomByName(roomName).ifPresent(r ->
                r.getDevices().stream()
                        .filter(d -> d.getType() == DeviceType.TEMPERATURE)
                        .findFirst()
                        .ifPresent(t -> t.setValue(tempC))
        );
    }
    public void setHumidityValue(String roomName, double humidity) {
        findRoomByName(roomName).ifPresent(r ->
                r.getDevices().stream()
                        .filter(d -> d.getType() == DeviceType.HUMIDITY)
                        .findFirst()
                        .ifPresent(h -> h.setValue(humidity))
        );
    }

    public Optional<Device> findFirstHeaterOrDehumidifierByRoomName(String roomName) {
        // using HEATER for dehumidifier in Keller
        return findRoomByName(roomName)
                .flatMap(r -> r.getDevices().stream()
                        .filter(d -> d.getType() == DeviceType.HEATER)
                        .findFirst());
    }


    // Heater
    public Optional<Device> findFirstHeaterByRoomName(String roomName) {
        return findRoomByName(roomName)
                .flatMap(r -> r.getDevices().stream()
                        .filter(d -> d.getType() == DeviceType.HEATER)
                        .findFirst());
    }

    public void setFirstHeaterInRoomByName(String roomName, boolean on) {
        findFirstHeaterByRoomName(roomName).ifPresent(h -> h.setOn(on));
    }

    // any device by type
    public Optional<Device> findFirstDeviceByTypeInRoomName(String roomName, DeviceType type) {
        return findRoomByName(roomName)
                .flatMap(r -> r.getDevices().stream()
                        .filter(d -> d.getType() == type)
                        .findFirst());
    }
}

