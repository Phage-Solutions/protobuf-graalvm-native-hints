![protobuf-graalvm-native-hints](readme-resources/graalvm-native-hints.png)

# Protobuf GraalVM Native Hints for Spring Boot 3

Simple library to register protobuf (v4) generated classes for runtime reflection. The library is [published to Maven Central](https://central.sonatype.com/artifact/sk.phage.spring/protobuf-graalvm-native-hints/overview), so there is no
need to add any additional repositories.

More details on the topic can be found [here](https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/#configuration-with-features)

## Content
- [Dependencies](#dependencies)
- [Usage](#usage)
- [Configuration](#usage)

## Dependencies

NOTE: See [build.gradle.kts](lib/build.gradle.kts) for the latest version of the dependencies.

List:
- Protocol Buffers 4.x.x
- GraalVM 17+
- Java 17+
- Spring Boot 3.x.x
- Reflections

## Usage

### Add the library to your project:
**Gradle (Groovy)**

```groovy
implementation 'sk.phage.spring:protobuf-graalvm-native-hints:${version}'
```

**Gradle (Kotlin DSL):**

```kotlin
implementation("sk.phage.spring:protobuf-graalvm-native-hints:${version}")
```

**Maven**

```xml
<dependency>
    <groupId>sk.phage.spring</groupId>
    <artifactId>protobuf-graalvm-native-hints</artifactId>
    <version>${version}</version>
</dependency>
```

### Usage
You need to register the class in your Spring Boot application via the `@ImportRuntimeHints` application.
```kotlin
@ImportRuntimeHints(ProtobufNativeHints::class)
```

During AOT compilation, the class will be picked up and all classes that extend the `GeneratedMessage` (incl. inner `Builder` classes, subtypes and `ProtocolMessageEnum`) will be registered for reflection (including all fields, constructors
and methods).

## Configuration
You will need to specify packages which contain your compiled ProtoBuf classes in the `META-INF/native-image/protobuf-packages.properties` file.

Example:
```properties
sk.phage.serialization
com.google.protobuf
```
