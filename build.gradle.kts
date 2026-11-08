plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    kotlin("kapt") version "2.3.21"

    `java-library`

    id("io.spring.dependency-management") version "1.1.7"

    id("org.jetbrains.dokka-javadoc") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "ru.sage"
version = "0.1.0"
description = "Spring Boot Starter for Sage Plugin Registry"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.2")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-web")

    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

    implementation("org.springframework.boot:spring-boot-starter-validation")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("ru.alexeypostnov", rootProject.name, version.toString())

    pom {
        name.set("Spring Boot Starter for Sage Plugin Registry")
        description.set("Spring Boot Starter for integration with Sage Plugin Registry")
        url.set("https://github.com/Alexey-Postnov888/spring-boot-starter-sage-plugin-registry")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Alexey-Postnov888")
                name.set("Alexey Postnov")
            }
        }
        scm {
            url.set("https://github.com/Alexey-Postnov888/spring-boot-starter-sage-plugin-registry")
            connection.set("scm:git:https://github.com/Alexey-Postnov888/spring-boot-starter-sage-plugin-registry.git")
            developerConnection.set("scm:git:ssh://git@github.com/Alexey-Postnov888/spring-boot-starter-sage-plugin-registry.git")
        }
    }
}