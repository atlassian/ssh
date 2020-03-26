package com.atlassian.performance.tools.ssh.port

import com.atlassian.performance.tools.ssh.api.Ssh
import com.atlassian.performance.tools.ssh.api.SshContainer
import com.atlassian.performance.tools.ssh.api.SshHost
import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LocalPortTest {

    @Test
    fun shouldForwardLocalPorts() {
        SshContainer().useSsh { ssh ->
            val localPort = 8022
            ssh.forwardLocalPort(
                localPort = localPort,
                remotePort = 22
            ).use {
                val result = Ssh(
                    SshHost(
                        ipAddress = "127.0.0.1",
                        userName = ssh.host.userName,
                        authentication = PublicKeyAuthentication(ssh.host.key),
                        port = localPort
                    )
                ).newConnection()
                    .use { it.execute("echo test") }

                assertThat(result.isSuccessful()).isTrue()
            }
        }
    }

}