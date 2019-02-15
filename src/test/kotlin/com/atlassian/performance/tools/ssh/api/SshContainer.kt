package com.atlassian.performance.tools.ssh.api


import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer

class SshContainer {
    fun run(action: (ssh: SshConnection) -> Unit) {
        SshUbuntuContainer().start().use { sshUbuntu ->
            return@use Ssh(
                sshUbuntu.ssh.toSshHost()
            ).newConnection()
                .use { action(it) }
        }
    }

    private fun com.atlassian.performance.tools.sshubuntu.api.SshHost.toSshHost(): SshHost {
        return SshHost(
            ipAddress = this.ipAddress,
            userName = this.userName,
            authentication = PublicKeyAuthentication(key = this.privateKey),
            port = this.port
        )
    }
}