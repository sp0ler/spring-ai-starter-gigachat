package ru.deevdenis.spring_ai_starter_gigachat.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.deevdenis.spring_ai_starter_gigachat.api.GigaChatServiceImpl;
import ru.deevdenis.spring_ai_starter_gigachat.model.GigaChatModel;
import ru.deevdenis.spring_ai_starter_gigachat.util.GigaChatProperties;
import ru.deevdenis.spring_ai_starter_gigachat.util.LoggingInterceptor;
import ru.gigachat.api.AuthorizationApi;
import ru.gigachat.api.BusinessApi;
import ru.gigachat.api.ChatApi;
import ru.gigachat.api.FilesApi;
import ru.gigachat.api.ModelsApi;
import ru.gigachat.invoker.ApiClient;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(GigaChatProperties.class)
public class AppAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    @Bean
    public GigaChatModel gigaChatModel(
            ObjectProvider<ObservationRegistry> observationRegistry, GigaChatServiceImpl gigaChatService
    ) {
        var obsRegistry = observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP);
        return new GigaChatModel(obsRegistry, gigaChatService, ToolCallingManager.builder().build());
    }

    @Bean
    @Scope("prototype")
    public ApiClient apiClient(RestClient restClient, ObjectMapper objectMapper, GigaChatProperties properties) {
        return new ApiClient(restClient, objectMapper, null)
                .setBasePath(properties.getUrl().getBase());
    }

    @Bean
    public ApiClient apiAuthClient(RestClient restClient, ObjectMapper objectMapper, GigaChatProperties properties) {
        return new ApiClient(restClient, objectMapper, null)
                .setBasePath(properties.getUrl().getAuth());
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public RestClient restClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // Define trust managers to accept all certificates
            TrustManager[] trustManagers = new TrustManager[]{new X509TrustManager() {
                // Method to check client's trust - accepting all certificates
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                }

                // Method to check server's trust - accepting all certificates
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                }

                // Method to get accepted issuers - returning an empty array
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            // Initialize SSL context with the defined trust managers
            sslContext.init(null, trustManagers, null);

            // Disable SSL verification for RestTemplate

            // Set the default SSL socket factory to use the custom SSL context
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // Set the default hostname verifier to allow all hostnames
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {}

        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                .requestInterceptors(interceptors -> interceptors.addAll(
                        List.of(new LoggingInterceptor()))
                ).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationApi authorizationApi(ApiClient apiAuthClient) {
        return new AuthorizationApi(apiAuthClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatApi chatApi(ApiClient apiClient) {
        return new ChatApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public FilesApi filesApi(ApiClient apiClient) {
        return new FilesApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelsApi modelsApi(ApiClient apiClient) {
        return new ModelsApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @Description("The bean is available only to legal entities that operate under the [pay-as-you-go] payment scheme.")
    public BusinessApi businessApi(ApiClient apiClient) {
        return new BusinessApi(apiClient);
    }
}
