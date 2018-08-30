val kotlinVersion = "1.2.30"

plugins {
    kotlin("jvm").version("1.2.30")
    id("com.atlassian.performance.tools.gradle-release").version("0.3.0")
}

configurations.all {
    resolutionStrategy.failOnVersionConflict()
    resolutionStrategy.eachDependency {
        when (requested.module.toString()) {
            "org.slf4j:slf4j-api" -> useVersion("1.8.0-alpha2")
        }
    }
}

dependencies {
    compile("com.atlassian.performance.tools:jvm-tasks:[1.0.0,2.0.0)")
    compile("com.atlassian.performance.tools:io:[1.0.0,2.0.0)")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlinVersion")
    compile("org.glassfish:javax.json:1.1")
    compile("com.hierynomus:sshj:0.23.0")
    listOf(
        "api",
        "core",
        "slf4j-impl"
    ).forEach { compile("org.apache.logging.log4j:log4j-$it:2.10.0") }
    testCompile("junit:junit:4.12")
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.9"
    distributionType = Wrapper.DistributionType.ALL
}
