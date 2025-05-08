package ru.deevdenis.spring_ai_starter_gigachat.api;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.deevdenis.spring_ai_starter_gigachat.util.GigaChatProperties;
import ru.gigachat.api.AuthorizationApi;
import ru.gigachat.api.BusinessApi;
import ru.gigachat.api.ChatApi;
import ru.gigachat.api.FilesApi;
import ru.gigachat.api.ModelsApi;
import ru.gigachat.model.AiCheck;
import ru.gigachat.model.AiCheckResponse;
import ru.gigachat.model.Balance;
import ru.gigachat.model.Chat;
import ru.gigachat.model.ChatCompletion;
import ru.gigachat.model.Embedding;
import ru.gigachat.model.EmbeddingsBody;
import ru.gigachat.model.FileDeleted;
import ru.gigachat.model.FileObject;
import ru.gigachat.model.Files;
import ru.gigachat.model.Models;
import ru.gigachat.model.Token;
import ru.gigachat.model.TokensCountBody;
import ru.gigachat.model.TokensCountInner;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GigaChatClient {

    private static final String BEARER_FORMAT = "Bearer %s";

    private final ChatApi chatApi;
    private final FilesApi filesApi;
    private final ModelsApi modelsApi;
    private final BusinessApi businessApi;
    private final AuthorizationApi authorizationApi;
    private final RestClient restClient;
    private final GigaChatProperties properties;

    @Cacheable(value = "gigachat-token")
    public Token getToken() {
        return authorizationApi.postToken(
                UUID.randomUUID().toString(),
                properties.getAuthorizationKey(),
                properties.getClientId(),
                properties.getScope()
        );
    }

    /**
     * Метод для отправки сообщения в гигачат.
     * @param chat сообщение в гигачат
     * @param xRequestId идентификатор запроса, для отслеживания состояния запроса в гиачате
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return ответ от гиачата.
     */
    public ChatCompletion chatCompletion(Chat chat, String xRequestId, String authorization) {
        return chatApi.postChat(
                properties.getClientId(),
                xRequestId,
                properties.getXSessionID(),
                BEARER_FORMAT.formatted(authorization),
                chat
        );
    }

    /**
     * Метод для получения остатка токенов пользователя на балансе
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return баланс пользователя
     */
    public Balance getBalance(String xRequestID, String authorization) {
        return chatApi.getBalance(xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод подчета количества токенов, по выбранной модели
     * @param tokensCountBody тело запроса с содержимым идентификатором модели
     * @param xRequestId идентификатор запроса
     * @param xSessionID идентификатор сессии
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return список объектов содержащих информацию о количестве токенов
     */
    public List<TokensCountInner> tokensCount(
            TokensCountBody tokensCountBody, String xRequestId, String xSessionID, String authorization
    ) {
        return chatApi.postTokensCount(xRequestId, xSessionID, BEARER_FORMAT.formatted(authorization), tokensCountBody);
    }

    /**
     * Метод для загрузки файлов в хранилище
     * @param file загружаемый файл
     * @param purpose назначение загружаемого файла
     * @param filename имя загружаемого файла
     * @param contentType тип контента загружаемого файла
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return возвращает объект с информацией о загруженном файле
     */
    public FileObject uploadFile(
            byte[] file, String xRequestID, String purpose, String filename, String contentType, String authorization
    ) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("purpose", purpose);
        multipartBodyBuilder.part("file", file)
                .contentType(MediaType.valueOf(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\"; filename=\"%s\"".formatted(filename));

        return restClient.post()
                .uri(properties.getUrl().getBase() + "files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(file.length)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_FORMAT.formatted(authorization))
                .header(HttpHeaders.USER_AGENT, "Java Spring-AI")
                .header("X-Request-ID", xRequestID)
                .header("X-Session-ID", properties.getXSessionID())
                .header("X-Client-ID", properties.getClientId())
                .body(multipartBodyBuilder.build())
                .retrieve()
                .body(FileObject.class);
    }

    /**
     * Получить информацию о файле Возвращает объект с описанием указанного файла
     * @param fileId идентификатор файла, который необходимо удалить
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return описание файла, доступного в хранилище
     */
    public FileObject getFile(String fileId, String xRequestID, String authorization) {
        return filesApi.getFile(fileId, xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод для удаления файла из хранилища.
     * @param fileId идентификатор файла, который необходимо удалить
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return ответ, содержащий информацию об успешном удалении файла
     */
    public FileDeleted fileDelete(String fileId, String xRequestID, String authorization) {
        return filesApi.fileDelete(
                fileId, xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод получения списка файлов
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return список файлов пользователя
     */
    public Files getFiles(String xRequestID, String authorization) {

        return filesApi.getFiles(xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод получения сгенерированного изображения с помощью AI
     * @param fileId идентификатор изображения
     * @param xClientID идентификатор клиента
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return сгенерированное изображение
     */
    public byte[] getFileId(String fileId, String xClientID, String xRequestID, String authorization) {
        return filesApi.getFileId(
                fileId, xClientID, xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод получения информации о доступных моделях
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @return список моделей, доступных пользователю
     */
    public Models getModels(String xRequestID, String authorization) {
        return modelsApi.getModels(xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization));
    }

    /**
     * Метод для проверки сгенерирован ли текст с помощью AI
     * @param xClientID идентификатор клиента
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @param aiCheck тело запроса с текстом для проверки
     * @return ответ на запрос проверки текста с помощью AI
     */
    public AiCheckResponse postAiCheck(String xClientID, String xRequestID, String authorization, AiCheck aiCheck) {

        return businessApi.postAiCheck(
                xClientID, xRequestID, properties.getXSessionID(), BEARER_FORMAT.formatted(authorization), aiCheck);
    }

    /**
     * Метод для получения эмбеддингов из текста
     * @param xRequestID идентификатор запроса
     * @param authorization авторизационный заголовок с токеном пользователя
     * @param embeddingsBody тело запроса с текстом для получения эмбеддингов
     * @return ответ на запрос с эмбеддингами текста
     */
    public Embedding postEmbeddings(String xRequestID, String authorization, EmbeddingsBody embeddingsBody) {
        return businessApi.postEmbeddings(xRequestID, properties.getXSessionID(), authorization, embeddingsBody);
    }

}
