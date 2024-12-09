plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"

    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("org.graalvm.nativeimage:svm:24.1.1")
    compileOnly("com.google.protobuf:protobuf-java:4.29.1")

    compileOnly("org.reflections:reflections:0.10.2")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.springframework.boot:spring-boot-starter:3.3.6")
}

group = "sk.phage.spring"
version = project.version

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Phage-Solutions/protobuf-graalvm-native-hints")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "sk.phage.spring"
            artifactId = rootProject.name

            from(components["java"])

            // Include the sources JAR
            artifact(tasks.named("sourcesJar").get())

            // Optionally include the JavaDocs JAR
            artifact(tasks.named("javadocJar").get())
        }
    }
}
