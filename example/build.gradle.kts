import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

group = "ru.deevdenis"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // spring-ai
    implementation("org.springframework.ai:spring-ai-core:1.0.0-M6")
    implementation("org.springframework.ai:spring-ai-spring-boot-autoconfigure:1.0.0-M6")
//    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-jdbc:1.0.0-M8")

    // my starters
    implementation("ru.deevdenis:spring-ai-starter-gigachat:0.0.1")
    implementation("ru.deevdenis:spring-ai-starter-model-chat-memory-jpa:0.0.1")

    implementation("org.postgresql:postgresql:42.7.5")
    implementation("org.liquibase:liquibase-core")

    // openapi
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.11.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.5")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val generateController = tasks.register<GenerateTask>("generateController") {
    description = "Generate controller"
    group = "controller"

    generatorName.set("spring")
    inputSpec.set("$projectDir/openapi/api.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
    apiPackage.set("ru.$group.api")
    modelPackage.set("ru.$group.model")
    invokerPackage.set("ru.$group.invoker")

    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "dateLibrary" to "java8-localdatetime",
        "useTags" to "true",
        "serializationLibrary" to "jackson",
        "useAbstractionForFiles" to "true",
        "generateSupportingFiles" to "true"
    ))
}
tasks.compileJava.get().dependsOn(generateController)

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/openapi/src/main/java")
        }
    }
}
