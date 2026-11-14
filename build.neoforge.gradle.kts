import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Tag nodes with `<mc version>` and `<loader>` so stonecutter.properties.toml
// sections like ["1.21.1"] resolve for both `1.21.1-fabric` and `1.21.1-neoforge`.
val (mcTag, loaderTag) = sc.current.project.split('-')
sc.properties.tags(mcTag, loaderTag)

plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("neoforge-mutex")
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("com.modrinth.minotaur") version "2.10.0"
}

version = "${sc.properties.get<String>("mod.version")}+${sc.current.version}-${loaderTag}"
group = sc.properties["mod.group"] as String
base { archivesName = sc.properties["mod.id"] as String }

// MC 26.1+ requires Java 25; the 1.21.x line runs on Java 21
val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:2.3.20"

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

neoForge {
    version = sc.properties["deps.neo_loader"] as String

    mods {
        register(sc.properties["mod.id"] as String) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            client()
            gameDirectory = file("../../run/")
            jvmArguments.addAll(listOf("-Xms1G", "-Xmx2G"))
        }
    }
}

dependencies {
    implementation(kotlinStdlib)

    // NeoForge does not bundle Kotlin at runtime - ship it inside the mod jar.
    // Newer NeoForge lines removed `additionalRuntimeClasspath` (implementation
    // already feeds the dev runtime there) and their stub configuration throws
    // on add, so both attachments are best-effort.
    for (configName in listOf("additionalRuntimeClasspath", "jarJar")) {
        try {
            project.dependencies.add(configName, kotlinStdlib)
        } catch (e: Exception) {
            logger.lifecycle("msi: configuration $configName unavailable on ${sc.current.project}: ${e.message?.lineSequence()?.first()}")
        }
    }
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
            exclude("org/fnzero/msi/fabric/**")
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
    // Stonecutter preprocessing must run before ModDevGradle produces Minecraft artifacts
    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    processResources {
        val props = buildMap {
            put("mod_id", sc.properties["mod.id"] as String)
            put("mod_name", sc.properties["mod.name"] as String)
            put("mod_license", sc.properties["mod.license"] as String)
            put("mod_authors", sc.properties["mod.authors"] as String)
            put("mod_description", sc.properties["mod.description"] as String)
            put("mod_version", "${sc.properties.get<String>("mod.version")}+${sc.current.version}-${loaderTag}")
            put("minecraft_version_range", sc.properties["mod.mc_compat"] as String)
            put("neoforge_version_range", sc.properties["deps.neo_range"] as String)
            put("loader_version_range", sc.properties["deps.fml_range"] as String)
        }
        inputs.properties(props)
        exclude("fabric.mod.json")
        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        val modVersion = sc.properties.get<String>("mod.version")
        description = "Copies the built jar into the root build/libs/$modVersion directory"
        from(named<Jar>("jar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modVersion"))
    }
}

// Publish this node's jar: `./gradlew :1.21.1-neoforge:modrinthPublish`, or every
// node at once with `./gradlew modrinthPublish`. Auth via the MODRINTH_TOKEN env
// var; MODRINTH_DEBUG=1 performs a dry run instead of uploading.
val mc_releases = (sc.properties["mod.mc_releases"] as String)
    .split(',')
    .map { it.trim() }
modrinth {
    projectId.set(sc.properties["modrinth.project"] as String)
    versionNumber.set("${sc.properties.get<String>("mod.version")}+${sc.current.version}-${loaderTag}")
    versionName.set("Mob Spawn Indicator ${sc.properties.get<String>("mod.version")} - MC ${mc_releases.joinToString("/")} ($loaderTag)")
    versionType.set("release")
    gameVersions.addAll(mc_releases)
    loaders.add(loaderTag)
    detectLoaders.set(false)
    uploadFile.set(tasks.named<Jar>("jar"))
    syncBodyFrom.set(rootProject.file("desc.md").readText())
    changelog.set(
        rootProject.file("changelog.md").takeIf { it.exists() }?.readText()
            ?: "Mob Spawn Indicator ${sc.properties.get<String>("mod.version")}"
    )
    debugMode.set(providers.environmentVariable("MODRINTH_DEBUG").isPresent())
}
