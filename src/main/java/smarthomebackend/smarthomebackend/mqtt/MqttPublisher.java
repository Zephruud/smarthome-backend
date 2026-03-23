package smarthomebackend.smarthomebackend.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {

    private final MqttClient client;

    public MqttPublisher(
            @Value("${mqtt.host}") String mqttHost,
            @Value("${mqtt.port}") int mqttPort,
            @Value("${mqtt.username:}") String mqttUsername,
            @Value("${mqtt.password:}") String mqttPassword
    ) throws MqttException {

        this.client = new MqttClient(
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
        System.out.println("✅ MQTT Publisher Connected");
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(1);
            client.publish(topic, msg);
            System.out.println("📤 MQTT Published | Topic: " + topic + " | Message: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}