import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/////////////////
// CHANGE ME
val starsectorDirectory = "C:/Program Files (x86)/Fractal Softworks/Starsector"
/////////////////

val starsectorCoreDirectory = "$starsectorDirectory/starsector-core"
val starsectorModDirectory = "$starsectorDirectory/mods"

plugins {
    kotlin("jvm") version "1.3.50"
    java
}

version = "1.0.0"

repositories {
    maven(url = uri("$projectDir/libs"))
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk7"))

    implementation("starfarer:starfarer-api:1.0")
    implementation(fileTree(starsectorCoreDirectory) {
        include("*.jar")
        exclude("starfarer.api.jar")
    })

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.mockk:mockk:1.9")
}

tasks {
    named<Jar>("jar")
    {
        destinationDir = file("$rootDir/jars")
        archiveName = "Gates_Awakened.jar"
    }

    register("debug-starsector", Exec::class) {
        println("Starting debugger for Starsector...")
        workingDir = file(starsectorCoreDirectory)


        commandLine = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            listOf("cmd", "/C", "starsectorDebug.bat")
        } else {
            listOf("./starsectorDebug.bat")
        }
    }

    register("run-starsector", Exec::class) {
        println("Starting Starsector...")
        workingDir = file(starsectorCoreDirectory)

        commandLine = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            listOf("cmd", "/C", "starsector.bat")
        } else {
            listOf("./starsector.bat")
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.6"
}