plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"

    id("java-library")
    id("maven-publish")
    id("signing")
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
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
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

            // Include the JavaDocs JAR
            artifact(tasks.named("javadocJar").get())

            pom {
                name.set("ProtoBuf GraalVM Native Image Spring Boot native hints")
                description.set("Native image hints for Spring Boot applications using ProtoBuf")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("daniel-phage")
                        name.set("Daniel Hladik")
                        email.set("daniel.hladik@phage.sk")
                        organization.set("Phage Solutions, s.r.o.")
                        organizationUrl.set("https://www.phage.sk")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PASSPHRASE")
    )
    sign(publishing.publications["maven"])
}
