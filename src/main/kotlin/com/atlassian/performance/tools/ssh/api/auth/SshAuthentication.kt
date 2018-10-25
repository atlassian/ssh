package com.atlassian.performance.tools.ssh.api.auth

import net.schmizz.sshj.SSHClient
import javax.json.JsonObject

abstract class SshAuthentication internal constructor() {
    internal abstract fun authenticate(userName: String, sshClient: SSHClient)
    internal abstract fun toJson(): JsonObject

    internal companion object {
        fun fromJson(
            json: JsonObject
        ): SshAuthentication {
            return when (json.getString("type")) {
                PasswordAuthentication.TYPE -> PasswordAuthentication(json)
                PublicKeyAuthentication.TYPE -> PublicKeyAuthentication(json)
                else -> {
                    throw IllegalStateException("Unknown authentication type ${json}")
                }
            }
        }
    }
}