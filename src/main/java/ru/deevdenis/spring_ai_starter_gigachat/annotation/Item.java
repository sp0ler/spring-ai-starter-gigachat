package ru.deevdenis.spring_ai_starter_gigachat.annotation;

public @interface Item {
    String key() default "";
    String value() default "";
}
