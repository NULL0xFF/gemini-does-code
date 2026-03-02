plugins {
    id("java-library")
}

dependencies {
    compileOnly(project(":plugin-api"))
    compileOnly("org.pf4j:pf4j:3.13.0")
    annotationProcessor("org.pf4j:pf4j:3.13.0")
}

tasks.jar {
    manifest {
        attributes(
            "Plugin-Class" to "com.null0xff.ark.plugin.sample.SamplePlugin",
            "Plugin-Id" to "sample-plugin",
            "Plugin-Version" to "0.0.1",
            "Plugin-Provider" to "null0xff"
        )
    }
}
