package sk.phage.graalvm.proto

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.ProtocolMessageEnum
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import java.io.IOException
import java.util.*

class ProtobufNativeHints : RuntimeHintsRegistrar {

    companion object {
        private const val PACKAGES_TO_SCAN_FILENAME: String = "META-INF/native-image/protobuf-packages.properties"
        private val LOG = LoggerFactory.getLogger(ProtobufNativeHints::class.java)
    }

    override fun registerHints(hint: RuntimeHints, classLoader: ClassLoader?) {
        val packagesToScan: Set<String>?

        try {
            packagesToScan = loadPackagesToScan()
            LOG.info("Loaded packages to scan:\n$packagesToScan")
        } catch (e: IOException) {
            throw RuntimeException("Failed to load packages to scan", e)
        }

        // register
        for (packageName in packagesToScan) {
            registerGrpcClassesFromReflection(hint, packageName)
        }
    }

    @Throws(IOException::class)
    private fun loadPackagesToScan(): Set<String> {
        val stream = javaClass
            .classLoader
            .getResourceAsStream(PACKAGES_TO_SCAN_FILENAME)

        if (stream == null) {
            throw RuntimeException("Resource not found: $PACKAGES_TO_SCAN_FILENAME")
        }

        return Properties().also { it.load(stream) }.stringPropertyNames()
    }

    private fun registerGrpcClassesFromReflection(hint: RuntimeHints, packageName: String) {
        val reflections = Reflections(packageName, Scanners.SubTypes)
        val messageClasses: Set<Class<out GeneratedMessage>> = reflections.getSubTypesOf(GeneratedMessage::class.java)
        val builderClasses: Set<Class<out GeneratedMessage.Builder<*>>> = reflections.getSubTypesOf(GeneratedMessage.Builder::class.java)
        val innerMessageClasses = messageClasses.flatMap { it.declaredClasses.toList() }
        val innerBuilderClasses = builderClasses.flatMap { it.declaredClasses.toList() }
        val enums: Set<Class<out ProtocolMessageEnum?>> = reflections.getSubTypesOf(ProtocolMessageEnum::class.java)

        val classesToBeRegistered: MutableSet<Class<*>> = HashSet()
        classesToBeRegistered.addAll(messageClasses)
        classesToBeRegistered.addAll(innerMessageClasses)
        classesToBeRegistered.addAll(innerBuilderClasses)
        classesToBeRegistered.addAll(builderClasses)
        classesToBeRegistered.addAll(enums)

        for (clazz in classesToBeRegistered) {
            registerClass(hint, clazz)
        }
    }

    private fun registerClass(hints: RuntimeHints, clazz: Class<*>) {
        val className = clazz.name

        try {
            // register class
            hints.reflection().registerType(clazz) {
                it.withMembers(*MemberCategory.entries.toTypedArray())
            }

            // Register default constructor if exists
            try {
                hints.reflection().registerConstructor(clazz.getDeclaredConstructor(), ExecutableMode.INVOKE)
            } catch (e: NoSuchMethodException) {
                // ignore
            }

            LOG.info("Registered class: [$className]")
        } catch (re: RuntimeException) {
            LOG.error("Failed to register class: [" + className + "] " + re.message)
        }
    }
}
