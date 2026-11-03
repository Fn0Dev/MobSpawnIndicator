pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Multi-version/multi-loader controller: https://stonecutter.kikugie.dev/
    id("dev.kikugie.stonecutter") version "0.9.8"

    // Applies the right Loom variant per Minecraft version (26.1+ vs older)
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // Lets Gradle auto-download JDK toolchains (Java 21 for 1.21.x, Java 25 for 26.1+)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        /**
         * Registers `<mc>-<loader>` nodes whose target version is `<mc>`,
         * each evaluated against its own per-loader build script.
         */
        fun match(version: String, vararg loaders: String) =
            loaders.forEach { version("$version-$it", version).buildscript = "build.$it.gradle.kts" }

        // Compile targets are the LAST version of each API-compat group;
        // in-between patches are covered by each node's declared version range.
        // NOTE: nodes are registered incrementally — a node's code must actually
        // compile before it joins the aggregate build.
        // Phase 4/5 roadmap (all 9 groups × both loaders):
        //   1.21 · 1.21.1 · 1.21.3[+1.21.2] · 1.21.4 · 1.21.5 ·
        //   1.21.8[+1.21.6,7] · 1.21.11[+1.21.9,10] · 26.1 · 26.2
        match("1.21.1", "fabric", "neoforge")

        vcsVersion = "1.21.1-neoforge"
    }
}

rootProject.name = "MobSpawnIndicator"
