plugins {
    `java-library`
    `maven-publish`
}

group = "ru.savelevvn"

// Read version from file
val versionFile = file("version")
version = if (versionFile.exists()) {
    versionFile.readText().trim()
} else {
    "1.0.0-SNAPSHOT"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot BOM для управления версиями
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.6"))

    // Jakarta BOM для управления Jakarta версиями
    implementation(platform("jakarta.platform:jakarta.jakartaee-bom:11.0.0"))

    // Spring Boot starters - compileOnly так как библиотека не должна включать их в себя
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")

    // Jakarta API - api так как они нужны пользователям библиотеки
    api("jakarta.persistence:jakarta.persistence-api")
    api("jakarta.validation:jakarta.validation-api")

    // Hibernate Validator - compileOnly так как это реализация, может быть другой
    compileOnly("org.hibernate.validator:hibernate-validator")

    // Spring Framework - api так как они нужны для работы с вашими классами
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-tx")
    api("org.springframework:spring-web")
    api("org.springframework:spring-webmvc")

    // Jakarta Servlet API
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    // Logging
    compileOnly("org.slf4j:slf4j-api")

    // Lombok - используем последнюю стабильную версию
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Vladnwx/spring-base-commons")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "ru.savelevvn"
            artifactId = "spring-base-commons"
            version = project.version.toString()

            from(components["java"])
        }
    }
}