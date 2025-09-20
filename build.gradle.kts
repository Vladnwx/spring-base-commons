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
    // Ваши зависимости остаются без изменений
    api("jakarta.persistence:jakarta.persistence-api:3.1.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    api("org.springframework.data:spring-data-jpa:3.1.0")
    api("org.springframework:spring-tx:6.0.0")
    api("org.springframework:spring-web:6.2.8")
    api("org.springframework:spring-webmvc:6.2.10")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("org.springframework.boot:spring-boot-starter-web:3.5.6")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
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