package ru.deevdenis.spring_ai_starter_gigachat.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@Data
@ConfigurationProperties(prefix = "gigachat")
public class GigaChatProperties {

    private String clientId;
    private String authorizationKey;
    private String scope;
    private String xSessionID = UUID.randomUUID().toString();
    private Url url = new Url();

    @Data
    public static class Url {
        private String auth = "https://ngw.devices.sberbank.ru:9443/api/v2/";
        private String base = "https://gigachat.devices.sberbank.ru/api/v1/";
    }
}
