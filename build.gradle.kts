import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.spring") version "1.5.30"
    id ("org.jetbrains.kotlin.plugin.jpa") version "1.5.21"
    id("org.springframework.boot") version "2.5.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id ("org.sonarqube") version "3.3"
    id("com.google.cloud.tools.jib") version "3.1.4"
    jacoco
}

group = "gg.op"
version = "0.0.26"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.google.api-client:google-api-client:1.32.1")
    implementation("org.mock-server:mockserver-netty:5.11.1")
    implementation("org.mock-server:mockserver-client-java:5.11.1")
    implementation("com.h2database:h2:1.4.200")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
    testImplementation("com.squareup.okhttp3:okhttp:4.9.1")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
}

springBoot {
    buildInfo()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "PACKAGE"

            limit {
                counter = "CLASS"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }

            excludes = listOf("gg.op.gameflix",
                "gg.op.gameflix.domain.game",
                "gg.op.gameflix.domain.user",
                "gg.op.gameflix.application.web.security",
                "gg.op.gameflix.infrastructure.epicgames",
                "gg.op.gameflix.infrastructure.steam",
                "gg.op.gameflix.application.document"
            )
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", rootProject.name)
        property("sonar.organization", "opgg-web-d")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

jib {
    from {
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/opgg-hackthon/${rootProject.name}"
    }
}