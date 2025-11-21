plugins {
    // To optionally create a shadow/fat jar that bundle up any non-core dependencies
    id("com.gradleup.shadow") version "8.3.5"
    // QuPath Gradle extension convention plugin
    id("qupath-conventions")
}

// Configure your extension here
qupathExtension {
    name = "qupath-extension-tseg"
    group = "io.github.qupath"
    version = "0.2.0"
    description = "Automatic Tumor Area Segmentation"
    automaticModule = "io.github.qupath.extension.tseg"
}

// TODO: Define your dependencies here
dependencies {
    // Main dependencies for most QuPath extensions
    shadow(libs.bundles.qupath)
    shadow(libs.bundles.logging)
    shadow(libs.qupath.fxtras)

    // For testing
    testImplementation(libs.bundles.qupath)
    testImplementation(libs.junit)
}
