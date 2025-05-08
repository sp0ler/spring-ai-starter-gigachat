package ru.deevdenis.spring_ai_starter_gigachat.util;

import lombok.experimental.UtilityClass;
import org.springframework.ai.chat.prompt.ChatOptions;
import ru.deevdenis.spring_ai_starter_gigachat.option.GigaChatOptions;

import java.util.Objects;

@UtilityClass
public class ToolUtils {

    public static boolean isToolsEnabled(ChatOptions options) {
        if (options instanceof GigaChatOptions gigaChatOptions) {
            return Objects.nonNull(gigaChatOptions.getInternalToolExecutionEnabled())
                    && gigaChatOptions.getInternalToolExecutionEnabled();
        }

        return false;
    }
}
