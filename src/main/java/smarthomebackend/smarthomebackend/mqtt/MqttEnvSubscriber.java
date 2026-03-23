package smarthomebackend.smarthomebackend.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

import java.nio.charset.StandardCharsets;

@Component
public class MqttEnvSubscriber {

    private final SmartHomeService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public MqttEnvSubscriber(
            SmartHomeService service,
            @Value("${mqtt.host}") String mqttHost,
            @Value("${mqtt.port}") int mqttPort,
            @Value("${mqtt.username:}") String mqttUsername,
            @Value("${mqtt.password:}") String mqttPassword
    ) throws MqttException {

        this.service = service;

        MqttClient client = new MqttClient(
                "tcp://" + mqttHost + ":" + mqttPort,
                MqttClient.generateClientId()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        if (!mqttUsername.isBlank()) {
            options.setUserName(mqttUsername);
        }
        if (!mqttPassword.isBlank()) {
            options.setPassword(mqttPassword.toCharArray());
        }

        client.connect(options);

        client.subscribe("home/env/living/state", (topic, msg) -> {
            try {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8).trim();
                JsonNode node = mapper.readTree(payload);
                if (node.has("tempC")) {
                    service.updateLivingTemperature(node.get("tempC").asDouble());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        client.subscribe("home/env/keller/state", (topic, msg) -> {
            try {
                String payload = new String(msg.getPayload(), StandardCharsets.UTF_8).trim();
                JsonNode node = mapper.readTree(payload);
                if (node.has("hum")) {
                    service.updateKellerHumidity(node.get("hum").asDouble());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}