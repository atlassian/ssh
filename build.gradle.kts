plugins {
    kotlin("jvm").version(Versions.kotlin)
    maven
}

maven {
    group = "com.atlassian.test.performance"
    version = "0.0.1-SNAPSHOT"
}

dependencies {
    compile(Libs.tasks)
    compile(Libs.io)
    compile(Libs.kotlinStandard)
    compile(Libs.json)
    compile("com.hierynomus:sshj:0.23.0")
    Libs.log4jCore().forEach { compile(it) }
}