package com.atlassian.performance.tools.ssh.api


import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import com.atlassian.performance.tools.sshubuntu.api.SshUbuntuContainer

internal class SshContainer {
    internal fun useConnection(action: (sshConnection: SshConnection) -> Unit) {
        SshUbuntuContainer().start().use { sshUbuntu ->
            return@use Ssh(
                sshUbuntu.ssh.toSshHost()
            ).newConnection()
                .use { action(it) }
        }
    }

    internal fun <T> useSsh(action: (ssh: Ssh) -> T): T {
        return SshUbuntuContainer().start().use { sshUbuntu ->
            action(Ssh(sshUbuntu.ssh.toSshHost()))
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