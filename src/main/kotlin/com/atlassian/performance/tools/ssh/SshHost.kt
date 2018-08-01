package com.atlassian.performance.tools.ssh

import java.nio.file.Path
import java.nio.file.Paths
import javax.json.Json
import javax.json.JsonObject

data class SshHost(
    val ipAddress: String,
    val userName: String,
    val key: Path
) {
    constructor(json: JsonObject) : this(
        ipAddress = json.getString("ipAddress"),
        userName = json.getString("userName"),
        key = Paths.get(json.getString("key"))
    )

    fun toJson(): JsonObject {
        return Json.createObjectBuilder()
            .add("ipAddress", ipAddress)
            .add("userName", userName)
            .add("key", key.toAbsolutePath().toString())
            .build()
    }
}