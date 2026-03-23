package smarthomebackend.smarthomebackend.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

@Component
public class MqttMotionSubscriber implements MqttCallback {

    private final MqttClient client;
    private final SmartHomeService service;

    public MqttMotionSubscriber(
            SmartHomeService service,
            @Value("${mqtt.host}") String mqttHost,
            @Value("${mqtt.port}") int mqttPort,
            @Value("${mqtt.username:}") String mqttUsername,
            @Value("${mqtt.password:}") String mqttPassword
    ) throws MqttException {

        this.service = service;

        this.client = new MqttClient(
                "tcp://" + mqttHost + ":" + mqttPort,
                "backendMotionSubscriber-" + MqttClient.generateClientId()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        if (!mqttUsername.isBlank()) {
            options.setUserName(mqttUsername);
        }
        if (!mqttPassword.isBlank()) {
            options.setPassword(mqttPassword.toCharArray());
        }

        client.setCallback(this);
        client.connect(options);
        client.subscribe("home/auto/motion", 1);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("❌ Motion subscriber connection lost: " + cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload()).trim().toUpperCase();
        service.applyMotionPayload(payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}