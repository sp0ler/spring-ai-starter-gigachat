package ru.deevdenis.spring_ai_starter_gigachat.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Item;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Parameters;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Property;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.Example;
import ru.deevdenis.spring_ai_starter_gigachat.annotation.ReturnParameters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class DateTimeTools {

    @Parameters
    @ReturnParameters(type = "string", properties = @Property(description = "Текущее время и дата"))
    @Example({"Какая сегодня дата?", "Какое сейчас время?"})
    @Tool(description = "Получить текущую дату и время в часовом поясе пользователя")
    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Parameters(type = "string", properties = @Property(description = "Временная зона"))
    @ReturnParameters(type = "string", properties = @Property(description = "Текущее время и дата"))
    @Example(value = {"Какая сейчас дата в Красноярске?", "Какое сейчас время в Новосибирске?"}, items = @Item(key = "zoneId", value = "Asia/Krasnoyarsk"))
    @Tool(description = "Получить текущую дату и время в указанном часовом поясе")
    public String getCurrentDateTimeWithZoneId(String zoneId) {
        return ZonedDateTime.now(ZoneId.of(zoneId)).toString();
    }
}
