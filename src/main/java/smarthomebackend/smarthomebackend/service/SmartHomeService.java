package smarthomebackend.smarthomebackend.service;

import org.springframework.stereotype.Service;
import smarthomebackend.smarthomebackend.model.Device;
import smarthomebackend.smarthomebackend.model.DeviceType;
import smarthomebackend.smarthomebackend.model.Room;
import smarthomebackend.smarthomebackend.mqtt.MqttPublisher;
import smarthomebackend.smarthomebackend.repo.SmartHomeRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SmartHomeService {

    private final SmartHomeRepository repo;
    private final MqttPublisher mqttPublisher;

    public SmartHomeService(SmartHomeRepository repo, MqttPublisher mqttPublisher) {
        this.repo = repo;
        this.mqttPublisher = mqttPublisher;
    }

    public List<Room> getRooms() {
        return repo.findAllRooms();
    }

    public Room getRoom(String roomId) {
        return repo.findRoomById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
    }

    public Room createRoom(String name) {
        Room room = repo.createRoom(name);
        mqttPublisher.publish("home/status", "Room created: " + name);
        return room;
    }

    public Device addDevice(String roomId, String name, DeviceType type) {
        Device d = repo.addDevice(roomId, name, type);
        mqttPublisher.publish("home/status", "Device added: " + name);
        return d;
    }

    // =========================
    // Save Energy Mode API
    // =========================
    public boolean isLivingSaveEnergyMode() {
        return repo.isLivingSaveEnergyMode();
    }

    public void setLivingSaveEnergyMode(boolean enabled) {
        repo.setLivingSaveEnergyMode(enabled);
        mqttPublisher.publish("home/status",
                "Save Energy Mode " + (enabled ? "ENABLED" : "DISABLED"));
    }
    public void updateKellerHumidity(double humidityPercent) {

        // ✅ thresholds (hysteresis)
        double HUMID_TOO_HIGH = 60.0; // turn ON
        double HUMID_OK       = 55.0; // turn OFF

        // 1) store humidity for Flutter display
        repo.setHumidityValue("Keller", humidityPercent);

        // 2) get devices
        Device dehumidifier = repo.findFirstHeaterOrDehumidifierByRoomName("Keller").orElse(null);
        if (dehumidifier == null) return;

        Device window = repo.findFirstDeviceByTypeInRoomName("Keller", DeviceType.WINDOW).orElse(null);
        boolean windowOpen = (window != null && window.isOn());

        // 3) only do automation when Save Energy Mode is enabled
        boolean saveEnergyEnabled = repo.isLivingSaveEnergyMode();
        if (!saveEnergyEnabled) return;

        // 4) window-open rule (optional, recommended)
        if (windowOpen) {
            if (dehumidifier.isOn()) {
                repo.setFirstHeaterInRoomByName("Keller", false);
                mqttPublisher.publish("home/status",
                        "Keller → Window open → dehumidifier disabled (auto OFF)");
            }
            return;
        }

        // 5) humidity control
        if (humidityPercent >= HUMID_TOO_HIGH && !dehumidifier.isOn()) {
            repo.setFirstHeaterInRoomByName("Keller", true);
            mqttPublisher.publish("home/status",
                    "Keller → Dehumidifier turned ON (auto, " + humidityPercent + "%)");
        } else if (humidityPercent <= HUMID_OK && dehumidifier.isOn()) {
            repo.setFirstHeaterInRoomByName("Keller", false);
            mqttPublisher.publish("home/status",
                    "Keller → Dehumidifier turned OFF (auto, " + humidityPercent + "%)");
        }
    }


    // =========================
    // called by MQTT env subscriber (Living sensor)
    // =========================
    public void updateLivingTemperature(double tempC) {
        double TOO_COLD = 20.0;
        double TOO_HOT  = 24.0;

        // ✅ Store temp into BOTH rooms (shared living sensor)
        repo.setTemperatureValue("Living Room", tempC);
        repo.setTemperatureValue("Bedroom", tempC);

        // ✅ Only run automation when save energy is ON
        boolean saveEnergyEnabled = repo.isSaveEnergyMode(); // uses the one flag
        if (!saveEnergyEnabled) return;

        // ✅ Apply same rules to BOTH rooms
        applyAutoHeatForRoom("Living Room", tempC, TOO_COLD, TOO_HOT);
        applyAutoHeatForRoom("Bedroom", tempC, TOO_COLD, TOO_HOT);
    }

    private void applyAutoHeatForRoom(String roomName, double tempC, double TOO_COLD, double TOO_HOT) {
        Device heater = repo.findFirstHeaterByRoomName(roomName).orElse(null);
        if (heater == null) return;

        Device window = repo.findFirstDeviceByTypeInRoomName(roomName, DeviceType.WINDOW).orElse(null);
        boolean windowOpen = (window != null && window.isOn()); // window ON = OPEN

        // ✅ Window-open rule
        if (windowOpen) {
            if (heater.isOn()) {
                repo.setFirstHeaterInRoomByName(roomName, false);
                mqttPublisher.publish("home/status",
                        roomName + " → Window open → heater disabled (auto OFF)");
            }
            return;
        }

        // ✅ Temperature thresholds
        if (tempC >= TOO_HOT && heater.isOn()) {
            repo.setFirstHeaterInRoomByName(roomName, false);
            mqttPublisher.publish("home/status",
                    roomName + " → Heater turned OFF (auto, " + tempC + "°C)");
        } else if (tempC <= TOO_COLD && !heater.isOn()) {
            repo.setFirstHeaterInRoomByName(roomName, true);
            mqttPublisher.publish("home/status",
                    roomName + " → Heater turned ON (auto, " + tempC + "°C)");
        }
    }

    public Device toggleDevice(String roomId, String deviceId, boolean on) {
        Room room = repo.findRoomById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));

        Device device = repo.findDevice(roomId, deviceId)
                .orElseThrow(() -> new NoSuchElementException("Device not found"));

        device.setOn(on);

        String msg = buildStatusMessage(room, device, on);
        mqttPublisher.publish("home/status", msg);

        return device;
    }

    public void deleteDevice(String roomId, String deviceId) {
        boolean deleted = repo.deleteDevice(roomId, deviceId);
        if (!deleted) {
            throw new NoSuchElementException("Device not found");
        }
        mqttPublisher.publish("home/status", "Device deleted: " + deviceId);
    }

    // Motion rules
    public void applyMotionPayload(String payload) {
        switch (payload) {
            case "KELLER_ON":
                repo.setFirstLightInRoomByName("Keller", true);
                mqttPublisher.publish("home/status", "AUTO: Keller light ON (motion)");
                break;

            case "KELLER_OFF":
                repo.setFirstLightInRoomByName("Keller", false);
                mqttPublisher.publish("home/status", "AUTO: Keller light OFF (no motion)");
                break;

            case "STORE_ON":
                repo.setFirstLightInRoomByName("Store Room", true);
                mqttPublisher.publish("home/status", "AUTO: Store light ON (motion)");
                break;

            case "STORE_OFF":
                repo.setFirstLightInRoomByName("Store Room", false);
                mqttPublisher.publish("home/status", "AUTO: Store light OFF (no motion)");
                break;

            default:
                System.out.println("⚠️ Unknown motion payload: " + payload);
                break;
        }
    }

    private String buildStatusMessage(Room room, Device device, boolean on) {
        String roomName = room.getName();
        String deviceName = device.getName();
        DeviceType type = device.getType();

        if (type == DeviceType.WINDOW) {
            return roomName + " → " + deviceName + (on ? " OPEN" : " CLOSED");
        }
        if (type == DeviceType.DOOR_LOCK) {
            return roomName + " → " + deviceName + (on ? " LOCKED" : " UNLOCKED");
        }
        return roomName + " → " + deviceName + (on ? " turned ON" : " turned OFF");
    }
}
