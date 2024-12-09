import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"

    id("java-library")
    id("com.vanniktech.maven.publish") version "0.30.0"
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    coordinates("sk.phage.spring", rootProject.name, project.version.toString())

    pom {
        name.set("ProtoBuf GraalVM Native Image Spring Boot native hints")
        description.set("Native image hints for Spring Boot applications using ProtoBuf")
        inceptionYear.set("2024")
        url.set("https://github.com/Phage-Solutions/protobuf-graalvm-native-hints")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set(System.getenv("POM_DEVELOPER_ID"))
                name.set(System.getenv("POM_DEVELOPER_NAME"))
                email.set(System.getenv("POM_DEVELOPER_EMAIL"))
                organization.set("Phage Solutions, s.r.o.")
                organizationUrl.set("https://www.phage.sk")
            }
        }

        scm {
            url.set("https://github.com/Phage-Solutions/protobuf-graalvm-native-hints")
            connection.set("scm:git:git://github.com/Phage-Solutions/protobuf-graalvm-native-hints.git")
            developerConnection.set("scm:git:ssh://git@github.com:Phage-Solutions/protobuf-graalvm-native-hints.git")
        }
    }
}
