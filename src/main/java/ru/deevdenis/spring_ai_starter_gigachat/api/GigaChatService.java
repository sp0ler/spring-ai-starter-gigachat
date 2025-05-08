package ru.deevdenis.spring_ai_starter_gigachat.api;

import ru.gigachat.model.Chat;
import ru.gigachat.model.ChatCompletion;
import ru.gigachat.model.FileObject;

/**
 * Основной интерфейс для работы с гиачатом.
 * @author Denis Deev
 */
public interface GigaChatService {

    /**
     * Метод для отправки сообщения в гигачат.
     * @param chat сообщение в гигачат
     * @param xRequestId идентификатор запроса, для отслеживания состояния запроса в гиачате
     * @return ответ от гиачата.
     */
    default ChatCompletion sendMessage(Chat chat, String xRequestId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Метод для загрузки файла в гиачат.
     * @param file байтовый массив файла
     * @param xRequestID идентификатор запроса, для отслеживания состояния запроса в гиачате
     * @param purpose назначение загружаемого файла, по умолчанию "general"
     * @param filename название загружаемого файла
     * @param contentType контент тип загружаемого файла
     * @return ответ от гиачата.
     */
    default FileObject uploadFile(byte[] file, String xRequestID, String purpose, String filename, String contentType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
