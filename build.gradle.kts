import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.2.70"
val log4jVersion = "2.17.1"

plugins {
    kotlin("jvm").version("1.2.70")
    id("com.atlassian.performance.tools.gradle-release").version("0.7.1")
}

configurations.all {
    resolutionStrategy {
        activateDependencyLocking()
        failOnVersionConflict()
        eachDependency {
            when (requested.module.toString()) {
                "org.slf4j:slf4j-api" -> useVersion("1.8.0-alpha2")
                "org.jetbrains:annotations" -> useVersion("15.0")
            }
            when (requested.group) {
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
                "org.apache.logging.log4j" -> useVersion(log4jVersion)
            }
        }
    }
}

dependencies {
    compile("com.atlassian.performance.tools:jvm-tasks:[1.0.0,2.0.0)")
    compile("com.atlassian.performance.tools:io:[1.0.0,2.0.0)")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile("org.glassfish:javax.json:1.1")
    compile("com.hierynomus:sshj:0.23.0")
    listOf(
        "api",
        "core",
        "slf4j-impl"
    ).forEach { compile("org.apache.logging.log4j:log4j-$it:$log4jVersion") }
    testCompile("junit:junit:4.13.2")
    testCompile("com.atlassian.performance.tools:ssh-ubuntu:0.2.6")
    testCompile("net.java.dev.jna:jna:5.8.0")
    testCompile("net.java.dev.jna:jna-platform:5.8.0")
}

tasks
    .withType(KotlinCompile::class.java)
    .forEach { compileTask ->
        compileTask.apply {
            kotlinOptions.apply {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjvm-default=enable")
            }
        }
    }

tasks.getByName("wrapper", Wrapper::class).apply {
    gradleVersion = "5.2.1"
    distributionType = Wrapper.DistributionType.ALL
}
