package smarthomebackend.smarthomebackend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    @Value("${mqtt.host}")
    private String mqttHost;

    @Value("${mqtt.port}")
    private int mqttPort;

    @Value("${mqtt.username:}")
    private String mqttUsername;

    @Value("${mqtt.password:}")
    private String mqttPassword;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{"tcp://" + mqttHost + ":" + mqttPort});
        options.setCleanSession(true);

        if (!mqttUsername.isBlank()) {
            options.setUserName(mqttUsername);
        }
        if (!mqttPassword.isBlank()) {
            options.setPassword(mqttPassword.toCharArray());
        }

        return options;
    }

    @Bean
    public DefaultMqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MqttPahoMessageHandler mqttOutbound() {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler("backendPublisher", mqttClientFactory());
        handler.setAsync(true);
        handler.setDefaultTopic("home/status");
        return handler;
    }
}