#!/usr/bin/env bash
# Builds every registered Stonecutter target (both loaders, all Minecraft
# versions) and collects the jars into build/libs/<mod version>/.
#
# The JDK situation is handled automatically:
#   - Gradle's daemon runs on JDK 25 (pinned by gradle/gradle-daemon-jvm.properties),
#     downloaded on demand via the foojay resolver, no matter what `java` the
#     launcher finds in PATH.
#   - Per-target toolchains (JDK 21 for 1.21.x, JDK 25 for 26.x) are likewise
#     provisioned automatically by settings.gradle.kts.
# So the only requirement is ANY Java installation for the Gradle launcher.
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1 && [ -z "${JAVA_HOME:-}" ]; then
  echo "error: no Java found - install any JDK (8+ for the launcher; the build" >&2
  echo "       itself fetches JDK 21/25 automatically)" >&2
  exit 1
fi

echo "==> Building all targets (this downloads JDKs and Minecraft artifacts on first run)"
./gradlew buildAndCollect "$@"

version_dir="build/libs/$(grep -oP '(?<=^mod.version = ")[^"]+' stonecutter.properties.toml)"
echo
echo "==> Done. Jars:"
ls -1 "$version_dir" 2>/dev/null || echo "(see versions/*/build/libs/)"
