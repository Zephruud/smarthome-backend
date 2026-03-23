package smarthomebackend.smarthomebackend.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {

    private final MqttClient client;

    public MqttPublisher() throws MqttException {
        // Same IP here
        this.client = new MqttClient("tcp://192.168.0.191:1883",
                MqttClient.generateClientId());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

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
