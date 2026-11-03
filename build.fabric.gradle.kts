import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Tag nodes with `<mc version>` and `<loader>` so stonecutter.properties.toml
// sections like ["1.21.1"] resolve for both `1.21.1-fabric` and `1.21.1-neoforge`.
val (mcTag, loaderTag) = sc.current.project.split('-')
sc.properties.tags(mcTag, loaderTag)

plugins {
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
}

version = "${sc.properties.get<String>("mod.version")}+${sc.current.version}"
group = sc.properties["mod.group"] as String
base { archivesName = sc.properties["mod.id"] as String }

// MC 26.1+ requires Java 25; the 1.21.x line runs on Java 21
val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:2.3.20"
val nightConfig = "com.electronwill.night-config:core:3.8.3"
val nightConfigToml = "com.electronwill.night-config:toml:3.8.3"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/releases")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // shared run directory between versions
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${sc.properties.get<String>("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    // Optional: only used by MsiModMenu, which Fabric Loader never loads without ModMenu
    modCompileOnly("com.terraformersmc:modmenu:${sc.properties.get<String>("deps.modmenu")}")
    // Mixin annotations for the world-render hook on versions where fabric-api
    // ships no world-render events (fabric-loader provides mixin at runtime)
    compileOnly("net.fabricmc:sponge-mixin:0.17.5+mixin.0.8.7")

    // Fabric does not ship Kotlin or NightConfig - bundle them into the mod jar
    implementation(kotlinStdlib)
    include(kotlinStdlib)
    implementation(nightConfig)
    implementation(nightConfigToml)
    include(nightConfig)
    include(nightConfigToml)
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

// The active node compiles shared sources raw (unpreprocessed), so the other
// loader's package must be kept out of compilation explicitly.
sourceSets {
    main {
        kotlin {
            exclude("org/fnzero/msi/neoforge/**")
        }
    }
}

// Stonecutter compiles the *active* node from the raw shared sources, where
// version-gated blocks for other targets would not resolve. Force every node
// (active included) to compile the preprocessed generated sources instead.
val generatedSources = layout.buildDirectory.dir("generated/stonecutter/main")
afterEvaluate {
    val main = sourceSets.getByName("main")
    val gen = generatedSources.get().asFile
    main.java.setSrcDirs(listOf(File(gen, "java")))
    main.resources.setSrcDirs(listOf(File(gen, "resources")))
    (main.extensions.findByName("kotlin") as? SourceDirectorySet)?.setSrcDirs(listOf(File(gen, "kotlin")))
}
tasks.matching { it.name in setOf("compileKotlin", "compileJava", "processResources") }.configureEach {
    dependsOn("stonecutterGenerate")
}

kotlin {
    compilerOptions {
        jvmTarget = when (requiredJava) {
            JavaVersion.VERSION_25 -> JvmTarget.JVM_25
            else -> JvmTarget.JVM_21
        }
    }
}

tasks {
    processResources {
        // The mixin config only exists on targets >= 1.21.9 (see the mixin class
        // gates); a string placeholder keeps the raw fabric.mod.json valid JSON
        // for Loom's mixin annotation processor.
        val mixinConfig =
            if (sc.current.parsed >= "1.21.9") "\"msi.mixins.json\"" else ""
        val props = buildMap {
            put("mod_id", sc.properties["mod.id"] as String)
            put("mod_name", sc.properties["mod.name"] as String)
            put("mod_license", sc.properties["mod.license"] as String)
            put("mod_authors", sc.properties["mod.authors"] as String)
            put("mod_description", sc.properties["mod.description"] as String)
            put("mod_version", sc.properties["mod.version"] as String)
            put("minecraft_version", sc.current.version)
            put("minecraft_compat_fabric", sc.properties.get<String>("mod.mc_compat_fabric"))
            put("java_version", requiredJava.majorVersion)
        }
        inputs.properties(props + mapOf("mixin_config" to mixinConfig))
        exclude("META-INF/neoforge.mods.toml")
        filesMatching("fabric.mod.json") {
            expand(props)
            filter { line -> line.replace("\"MSI_MIXIN_CONFIGS\"", mixinConfig) }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        val modVersion = sc.properties.get<String>("mod.version")
        description = "Copies the built jar into the root build/libs/$modVersion directory"
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modVersion"))
    }
}
