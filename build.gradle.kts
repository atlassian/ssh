import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.2.70"
val log4jVersion = "[2.0.0, 2.999.999)"
val log4jGroup = "org.apache.logging.log4j"

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
                "org.jetbrains:annotations" -> useVersion("15.0")
                // conflict between testcontainers, ssh-ubuntu and sshj
                "org.slf4j:slf4j-api" -> useVersion("1.7.25")
            }
            when (requested.group) {
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
                // conflict between jvm-tasks and ssh-ubuntu
                log4jGroup -> useVersion(log4jVersion)
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
    api("$log4jGroup:log4j-api:$log4jVersion")
    testImplementation("$log4jGroup:log4j-core:$log4jVersion")
    testCompile("junit:junit:4.12")
    testCompile("com.atlassian.performance.tools:ssh-ubuntu:0.1.0")
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
