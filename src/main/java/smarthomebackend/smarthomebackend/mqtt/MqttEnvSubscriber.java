package smarthomebackend.smarthomebackend.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

import java.nio.charset.StandardCharsets;

@Component
public class MqttEnvSubscriber {

    private final SmartHomeService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public MqttEnvSubscriber(SmartHomeService service) throws MqttException {
        this.service = service;

        MqttClient client = new MqttClient(
                "tcp://192.168.0.191:1883",
                MqttClient.generateClientId()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);

        // ✅ 1) Living temperature (existing)
        client.subscribe("home/env/living/state", (topic, msg) -> {
            try {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8).trim();

                // {"room":"living","tempC":23.45,"ts":123456}
                JsonNode node = mapper.readTree(payload);

                if (node.has("tempC")) {
                    double tempC = node.get("tempC").asDouble();
                    service.updateLivingTemperature(tempC);
                } else {
                    System.out.println("⚠️ living/state missing tempC: " + payload);
                }

            } catch (Exception e) {
                System.out.println("❌ Error parsing living env payload");
                e.printStackTrace();
            }
        });

        // ✅ 2) Keller humidity (NEW)
        client.subscribe("home/env/keller/state", (topic, msg) -> {
            try {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8).trim();

                // Example:
                // {"room":"keller","hum":62.5,"ts":123456}
                JsonNode node = mapper.readTree(payload);

                if (node.has("hum")) {
                    double hum = node.get("hum").asDouble();
                    service.updateKellerHumidity(hum);
                } else {
                    System.out.println("⚠️ keller/state missing hum: " + payload);
                }

            } catch (Exception e) {
                System.out.println("❌ Error parsing keller env payload");
                e.printStackTrace();
            }
        });

        System.out.println("✅ MQTT ENV Subscriber Connected:");
        System.out.println("   - home/env/living/state");
        System.out.println("   - home/env/keller/state");
    }
}
