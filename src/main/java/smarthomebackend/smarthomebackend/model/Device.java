package smarthomebackend.smarthomebackend.model;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




public class Device {
    private String id;
    private String name;
    private DeviceType type;
    private boolean on;

    // ✅ NEW: used for TEMPERATURE sensor value (°C)
    private Double value;

    public Device() {}

    public Device(String id, String name, DeviceType type, boolean on) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.on = on;
    }

    // --- getters/setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    public boolean isOn() { return on; }
    public void setOn(boolean on) { this.on = on; }

    // ✅ NEW
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
