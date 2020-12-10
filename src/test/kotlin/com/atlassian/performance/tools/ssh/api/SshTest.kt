package com.atlassian.performance.tools.ssh.api

import org.junit.Test

class SshTest {

    @Test
    fun shouldDetachProcess() {
        SshContainer().useSsh { sshHost ->
            installPing(sshHost)

            val ping = sshHost.newConnection().use { ssh ->
                ssh.startProcess("ping localhost")
            }
            sshHost.newConnection().use { ssh ->
                ssh.stopProcess(ping)
            }
        }
    }

    private fun installPing(sshHost: Ssh) {
        sshHost.newConnection().use { it.execute("apt-get update -qq && apt-get install iputils-ping -y") }
    }
}
