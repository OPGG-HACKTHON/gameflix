import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    id ("org.jetbrains.kotlin.plugin.jpa") version "1.5.21"
    id("org.springframework.boot") version "2.5.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id ("org.sonarqube") version "3.3"
    id("com.google.cloud.tools.jib") version "3.1.4"
    jacoco
}

group = "gg.op"
version = "0.0.2"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.9")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.9")
    implementation("com.h2database:h2:1.4.200")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
                minimum = "1.00".toBigDecimal()
            }

            excludes = listOf("gg.op.gameflix",
                "gg.op.gameflix.domain.game",
                "gg.op.gameflix.domain.user",
                "gg.op.gameflix.infrastructure.igdb",
                "gg.op.gameflix.infrastructure.blizzard",
                "gg.op.gameflix.infrastructure.epicgames",
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
        }
    }
    to {
        image = "ghcr.io/opgg-hackthon/${rootProject.name}"
    }
}