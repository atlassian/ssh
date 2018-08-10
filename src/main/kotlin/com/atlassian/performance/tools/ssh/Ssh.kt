package com.atlassian.performance.tools.ssh

import com.atlassian.performance.tools.jvmtasks.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.IdempotentAction
import net.schmizz.sshj.SSHClient
import java.time.Duration

data class Ssh @JvmOverloads constructor(
    val host: SshHost,
    private val connectivityPatience: Int = 4
) {
    fun newConnection(): SshConnection {
        return SshConnection(
            prepareClient(),
            host.userName
        )
    }

    private fun prepareClient(): SSHClient {
        val ssh = SSHClient()
        ssh.connection.keepAlive.keepAliveInterval = 60
        ssh.addHostKeyVerifier { _, _, _ -> true }
        waitForConnectivity(ssh)
        ssh.authPublickey(host.userName, host.key.toString())
        return ssh
    }

    private fun waitForConnectivity(
        ssh: SSHClient
    ) {
        val address = host.ipAddress
        IdempotentAction("connect to $address") { ssh.connect(address) }
            .retry(
                maxAttempts = connectivityPatience,
                backoff = ExponentialBackoff(
                    baseBackoff = Duration.ofSeconds(1)
                )
            )
    }
}