import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
    id("maven-publish")
}

group = "ru.deevdenis"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }

    all {
        exclude("org.springframework.boot:spring-boot-starter-logging")
        exclude("org.apache.logging.log4j:log4j-slf4j2-impl")
        exclude("org.apache.logging.log4j:log4j-to-slf4j")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.spring.io/milestone")
    maven(url = "https://repo.spring.io/snapshot")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    // spring-ai
    implementation("org.springframework.ai:spring-ai-spring-boot-autoconfigure:1.0.0-M6")

    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-jdbc:1.0.0-M8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.5")

    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    implementation("org.springframework.boot:spring-boot-starter")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}
