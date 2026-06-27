import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("net.neoforged.moddev") version "2.0.134"
  id("org.jetbrains.kotlin.jvm") version "2.3.20"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("mod_group").get()
base { archivesName = providers.gradleProperty("mod_id").get() }

val kotlin_version = "2.3.20"
val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"

repositories {
  mavenCentral()
  maven("https://maven.neoforged.net/releases")
}

neoForge {
  version = providers.gradleProperty("neoforge_version").get()

  runs {
    create("client") {
      client()
      jvmArguments.addAll(listOf("-Xms1G", "-Xmx2G"))

    }

    configureEach {
      logLevel = org.slf4j.event.Level.DEBUG
    }
  }

  mods {
    register(providers.gradleProperty("mod_id").get()) {
      sourceSet(sourceSets.main.get())
    }
  }
}

dependencies {
  implementation(kotlin_stdlib)
  add("additionalRuntimeClasspath", kotlin_stdlib)
  jarJar(kotlin_stdlib)
  
  compileOnly("org.spongepowered:mixin:0.8.7")
}

tasks.processResources {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  val replace_properties =
      mapOf(
          "version" to version,
          "mod_version" to version,
          "mod_id" to providers.gradleProperty("mod_id").get(),
          "mod_name" to providers.gradleProperty("mod_name").get(),
          "mod_license" to providers.gradleProperty("mod_license").get(),
          "mod_authors" to providers.gradleProperty("mod_authors").get(),
          "mod_description" to providers.gradleProperty("mod_description").get(),
          "minecraft_version_range" to providers.gradleProperty("minecraft_version_range").get(),
          "neoforge_version_range" to providers.gradleProperty("neoforge_version_range").get(),
          "loader_version_range" to providers.gradleProperty("loader_version_range").get(),
      )
  inputs.properties(replace_properties)
  filesMatching("META-INF/neoforge.mods.toml") { expand(replace_properties) }
}

tasks.withType<JavaCompile>().configureEach { options.release = 21 }

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}
