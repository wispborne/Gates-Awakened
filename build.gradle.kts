import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems.jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/////////////////
// CHANGE ME
val starsectorCoreDirectory = "C:/Program Files (x86)/Fractal Softworks/Starsector/starsector-core"
/////////////////

plugins {
    kotlin("jvm") version "1.3.11"
    java
}

version = "1.0.0"

repositories {
    jcenter()
}

dependencies {
    compile(fileTree(starsectorCoreDirectory) { include("*.jar")})
    compile(kotlin("stdlib-jdk7"))
}

tasks {
    named<Jar>("jar")
    {
        destinationDir = file("$rootDir/jars")
        archiveName = "Active_Gates.jar"
    }

    register("debug-starsector", Exec::class) {
        println("Starting debugger for Starsector...")
        workingDir = file(starsectorCoreDirectory)
        commandLine = listOf("cmd", "/C", "starsectorDebug.bat")
    }

    register("run-starsector", Exec::class) {
        println("Starting Starsector...")
        workingDir = file(starsectorCoreDirectory)
        commandLine = listOf("cmd", "/C", "starsector.bat")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.6"
}