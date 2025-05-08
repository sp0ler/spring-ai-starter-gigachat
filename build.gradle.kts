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
	implementation("org.springframework.ai:spring-ai-core:1.0.0-M6")
	implementation("org.springframework.ai:spring-ai-spring-boot-autoconfigure:1.0.0-M6")

	// openapi
	implementation("org.openapitools:openapi-generator-gradle-plugin:7.11.0")
	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	compileOnly("javax.servlet:javax.servlet-api:4.0.1")

	implementation("com.google.guava:guava:33.4.6-jre")
	implementation("org.apache.commons:commons-lang3:3.17.0")

	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
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

apply(plugin = "org.openapi.generator")

val generateGigachat = tasks.register<GenerateTask>("generateGigachat") {
	description = "Generate GigaChat RestClient"
	group = "gigachat"

	generatorName.set("java")
	inputSpec.set("$projectDir/openapi/gigachat.yml")
	outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
	apiPackage.set("ru.$group.api")
	modelPackage.set("ru.$group.model")
	invokerPackage.set("ru.$group.invoker")

	configOptions.set(mapOf(
		"library" to "restclient",
		"dateLibrary" to "java8-localdatetime",
	))
}
tasks.compileJava.get().dependsOn(generateGigachat)

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

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = group.toString()
			artifactId = "spring-ai-starter-gigachat"
			version = version

			from(components["java"])
		}
	}
}
