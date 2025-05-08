package ru.deevdenis.spring_ai_starter_gigachat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gigachat.model.Chat;
import ru.gigachat.model.ChatCompletion;
import ru.gigachat.model.FileObject;
import ru.gigachat.model.Token;

@Service
@RequiredArgsConstructor
public class GigaChatServiceImpl implements GigaChatService {

    private final GigaChatClient client;

    @Override
    public ChatCompletion sendMessage(Chat chat, String xRequestId) {
        Token token = client.getToken();
        return client.chatCompletion(chat, xRequestId, token.getAccessToken());
    }

    @Override
    public FileObject uploadFile(byte[] file, String xRequestID, String purpose, String filename, String contentType) {
        Token token = client.getToken();
        return client.uploadFile(file, xRequestID, purpose, filename, contentType, token.getAccessToken());
    }
}
