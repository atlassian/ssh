package com.atlassian.performance.tools.ssh.api.auth

import net.schmizz.sshj.SSHClient
import java.nio.file.Path
import java.nio.file.Paths
import javax.json.Json
import javax.json.JsonObject

data class PublicKeyAuthentication(internal val key: Path) : SshAuthentication() {
    companion object {
        const val TYPE = "public-key"
    }

    internal constructor(json: JsonObject) :
        this(Paths.get(json.getString("value")))

    override fun toJson(): JsonObject {
        return Json.createObjectBuilder()
            .add("type", "public-key")
            .add("value", key.toAbsolutePath().toString())
            .build()
    }

    override fun authenticate(userName: String, sshClient: SSHClient) {
        sshClient.authPublickey(userName, key.toString())
    }
}