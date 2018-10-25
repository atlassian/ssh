package com.atlassian.performance.tools.ssh.api.auth

import net.schmizz.sshj.SSHClient
import javax.json.Json
import javax.json.JsonObject

data class PasswordAuthentication(private val password: String) : SshAuthentication() {
    internal companion object {
        const val TYPE = "password"
    }

    internal constructor(json: JsonObject) :
        this(json.getString("value"))


    override fun toJson(): JsonObject {
        return Json.createObjectBuilder()
            .add("type", TYPE)
            .add("value", password)
            .build()
    }

    override fun authenticate(userName: String, sshClient: SSHClient) {
        sshClient.authPassword(userName, password)
    }
}