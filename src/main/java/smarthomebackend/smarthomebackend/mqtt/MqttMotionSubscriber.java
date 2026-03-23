package smarthomebackend.smarthomebackend.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;
import smarthomebackend.smarthomebackend.service.SmartHomeService;

@Component
public class MqttMotionSubscriber implements MqttCallback {

    private final MqttClient client;
    private final SmartHomeService service;

    public MqttMotionSubscriber(SmartHomeService service) throws MqttException {
        this.service = service;

        this.client = new MqttClient(
                "tcp://192.168.0.191:1883",
                "backendMotionSubscriber-" + MqttClient.generateClientId()
        );

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.setCallback(this);
        client.connect(options);

        client.subscribe("home/auto/motion", 1);
        System.out.println("✅ MQTT Motion Subscriber Connected + Subscribed: home/auto/motion");
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("❌ Motion subscriber connection lost: " + cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload()).trim().toUpperCase();
        System.out.println("📥 MOTION MQTT | Topic: " + topic + " | Payload: " + payload);

        // KELLER_ON, KELLER_OFF, STORE_ON, STORE_OFF
        service.applyMotionPayload(payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used (subscriber)
    }
}

