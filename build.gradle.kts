plugins {
    `java-library`
    `maven-publish`
}

group = "ru.savelevvn"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Jakarta Persistence API
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Spring Data JPA (для аннотаций аудита и JpaRepository)
    api("org.springframework.data:spring-data-jpa:3.1.0")

    // Spring Transaction
    api("org.springframework:spring-tx:6.0.0")

    // Spring Web (для ResponseEntity и аннотаций контроллеров)
    api("org.springframework:spring-web:6.2.8")

    // Spring Web MVC (для Thymeleaf контроллера)
    api("org.springframework:spring-webmvc:6.2.10")

    // Servlet API
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // SLF4J для логирования
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    compileOnly("org.springframework.boot:spring-boot-starter-web:3.5.6")
    // Для тестов
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Настройка публикации
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Vladnwx/spring-base-commons")
            credentials {
                username = System.getenv("USERNAME") ?: project.findProperty("gpr.user") as String? ?: ""
                password = System.getenv("TOKEN") ?: project.findProperty("gpr.key") as String? ?: ""
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "ru.savelevvn"
            artifactId = "spring-base-commons"
            version = version

            from(components["java"])
        }
    }
}