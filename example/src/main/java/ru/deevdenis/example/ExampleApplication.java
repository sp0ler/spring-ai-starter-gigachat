package ru.deevdenis.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EntityScan(basePackages = {"ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.entity"})
//@EnableJpaRepositories(basePackages = {"ru.deevdenis.spring_ai_starter_model_chat_memory_jpa.repository"})
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

}
