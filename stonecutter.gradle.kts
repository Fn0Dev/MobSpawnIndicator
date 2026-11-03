plugins {
    id("dev.kikugie.stonecutter")
}

// No active node: every node compiles preprocessed sources, so version-gated
// code stays correct on every target (the active node would compile raw).
stonecutter active "1.21.1-neoforge"

// https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    // Loader constants, usable in versioned comments: `//? if fabric {` / `//? if neoforge {`
    constants["fabric"] = current.project.endsWith("fabric")
    constants["neoforge"] = current.project.endsWith("neoforge")
}
