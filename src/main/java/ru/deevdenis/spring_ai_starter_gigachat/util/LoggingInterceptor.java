package ru.deevdenis.spring_ai_starter_gigachat.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(
                    """
                    \n===========request==========
                    URL: {},
                    Method: {},
                    Headers: {},
                    Body: {}
                    ============================
                    """,
                    request.getURI(), request.getMethod(), request.getHeaders(), new String(body, StandardCharsets.UTF_8)
            );
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("""
                        \n==========response==========
                        StatusCode: {},
                        StatusText: {},
                        Headers: {},
                        Body: {}
                        ============================
                        """,
                    response.getStatusCode(), response.getStatusText(), response.getHeaders(), StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8)
            );
        }
    }
}
