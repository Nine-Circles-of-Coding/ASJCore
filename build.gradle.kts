
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask

plugins {
    // The GTNH convention has no auto-applying Kotlin module (only Scala), but it does put the
    // Kotlin plugin on the buildscript classpath, so apply it without a version to use the
    // version the convention pins (2.2.21 as of gtnhgradle 2.0.20).
    kotlin("jvm")
    // Runs the Mixin annotation processor over the Kotlin sources, so the mixins can be written in
    // Kotlin alongside the handlers they call. Both halves of the toolchain cooperate on this:
    // gtnhgradle's MixinModule adds the mixin provider to the `kapt` configuration, and RFG's
    // ModUtils fills in KaptExtension.javacOptions with the reobfSrgFile/outRefMapFile arguments
    // that make the refmap - and therefore obfuscated runs - work.
    kotlin("kapt")
    id("com.gtnewhorizons.gtnhconvention")
}

kotlin {
    compilerOptions {
        // 1.7.10 targets Java 8, same as the old build's compileKotlin.kotlinOptions.jvmTarget
        jvmTarget = JvmTarget.JVM_1_8
    }
}

// The Mixin annotation processor writes its target database to java.io.tmpdir. kapt runs the
// processor in a forked worker whose environment has no TEMP/TMP, so that resolves to C:\Windows on
// Windows and the write fails. RFG already creates build/tmp/mixins and points the *javac* path at
// it, but a system property set in the Gradle daemon does not reach the worker - so pass it through.
// Only the main source set's kapt task: RFG registers build/tmp/mixins/mixins.srg as an output of
// every KaptTask it sees, so realizing kaptTestKotlin as well makes Gradle report a duplicate-output
// dependency between it and reobfJar.
tasks.withType<KaptWithoutKotlincTask>().configureEach {
    // RFG registers build/tmp/mixins/mixins.srg as an output of *every* KaptTask, so the main and
    // test tasks claim the same file and Gradle flags the overlap against reobfJar. Mixins are only
    // ever written in the main source set, so the processor has nothing to do on the test one.
    if (name != "kaptKotlin") {
        enabled = false
        return@configureEach
    }
    val mixinTmp = layout.buildDirectory.dir("tmp/mixins")
    doFirst { mixinTmp.get().asFile.mkdirs() }
    kaptProcessJvmArgs.add(mixinTmp.map { "-Djava.io.tmpdir=${it.asFile.absolutePath}" })
}

// A missing or empty refmap is invisible in a dev run and fatal in production: every mixin that
// targets a vanilla member silently fails to apply once the game is obfuscated. The kapt/RFG
// interaction has been observed to drop build/tmp/mixins on some incremental builds, so assert the
// packaged refmap actually has mappings whenever any mixin source declares an injector. Recover by
// deleting build/tmp/mixins and rebuilding.
val verifyMixinRefmap = tasks.register("verifyMixinRefmap") {
    val mixinRoots = files("src/main/kotlin/alexsocol/mixins", "src/main/java/alexsocol/mixins")
    val mixinSources = mixinRoots.asFileTree.matching { include("**/*.kt", "**/*.java") }
    val refmap = layout.buildDirectory.file("resources/main/mixins.asjlib.refmap.json")
    val pkgRoot = "alexsocol/mixins/"
    inputs.files(mixinSources).withPropertyName("mixinSources")
    // Cheap, and the thing it guards against is a *disappearing* output, so never skip it.
    outputs.upToDateWhen { false }
    doLast {
        val injectors = listOf(
            "@Inject", "@ModifyReturnValue", "@Redirect", "@ModifyArg", "@ModifyArgs",
            "@ModifyVariable", "@ModifyConstant", "@WrapOperation", "@WrapWithCondition"
        )
        // Only sources whose targets actually remap can be expected in the refmap. A file that
        // mentions remap = false anywhere is skipped rather than guessed at.
        //
        // Class names come from the file's contents, not its path: several mixins share a file
        // (EntityMixins.kt and friends), and Kotlin does not require the two to match.
        val classDecl = Regex("""\bclass\s+(Mixin\w+)""")
        // A method name that actually needs remapping: not a constructor or static initialiser,
        // which are JVM-reserved and identical in every mapping.
        val remappable = Regex("""method\s*=\s*[\[\s]*"(?!<)([^"(]+)""")
        val expected = mixinSources.files
            .filter { f -> f.readText().let { t -> injectors.any(t::contains) && !t.contains("remap = false") } }
            .flatMap { f ->
                val dir = f.absolutePath.replace('\\', '/').substringAfter(pkgRoot).substringBeforeLast('/', "")
                val prefix = pkgRoot + if (dir.isEmpty()) "" else "$dir/"
                val text = f.readText()
                // Several mixin classes can share one file, so attribute each injector to the class
                // it sits in by slicing the source at class declarations.
                val decls = classDecl.findAll(text).toList()
                decls.mapIndexedNotNull { i, m ->
                    val body = text.substring(m.range.last, decls.getOrNull(i + 1)?.range?.first ?: text.length)
                    if (remappable.containsMatchIn(body)) prefix + m.groupValues[1] else null
                }
            }
            .distinct()
            .sorted()
        if (expected.isEmpty()) return@doLast

        val file = refmap.get().asFile
        if (!file.isFile) error(
            "Mixin refmap is missing (${file}), but ${expected.size} mixin source(s) declare remappable " +
                "injectors. Obfuscated runs would silently fail to inject. Delete build/tmp/mixins and rebuild."
        )
        val text = file.readText()
        val missing = expected.filterNot { text.contains("\"$it\"") }
        if (missing.isNotEmpty()) error(
            "Mixin refmap ${file} is missing entries for:\n" + missing.joinToString("\n") { "  $it" } +
                "\nObfuscated runs would silently fail to inject these. Delete build/tmp/mixins and rebuild."
        )
    }
}

tasks.named("jar") { dependsOn(verifyMixinRefmap) }
verifyMixinRefmap { dependsOn(tasks.named("processResources")) }

dependencies {
    // Shipped inside the jar, unrelocated: ASJCore's KotlinAdapter provides the Kotlin
    // runtime for this mod and its dependents, so the real kotlin.** names must survive.
    // Version is taken from the Kotlin plugin above so the two cannot drift apart.
    "shadowImplementation"(kotlin("stdlib-jdk8"))
}

tasks.named<Jar>("shadowJar") {
    // Matches the exclusions from the old build.gradle's embed-into-jar step.
    // The annotations are compile-time only (CLASS retention), so dropping them is safe.
    exclude("org/intellij/lang/annotations/**")
    exclude("org/jetbrains/annotations/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
}
