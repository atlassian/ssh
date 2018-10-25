package com.atlassian.performance.tools.ssh.api


import com.atlassian.performance.tools.ssh.api.auth.PasswordAuthentication
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

class SshContainer {
    fun run(action: (ssh: SshConnection) -> Unit) {
        val sshPort = 22
        GenericContainerImpl("rastasheep/ubuntu-sshd:16.04")
            .withExposedPorts(sshPort)
            .waitingFor(Wait.forListeningPort()).use { ubuntuContainer ->
                ubuntuContainer.start()
                val mappedSshPort = ubuntuContainer.getMappedPort(sshPort)
                val ssh = Ssh(
                    SshHost(
                        ipAddress = ubuntuContainer.containerIpAddress,
                        userName = "root",
                        port = mappedSshPort,
                        authentication = PasswordAuthentication("root")
                    )
                )
                val sshConnection = ssh.newConnection()
                action(sshConnection)
            }
    }
}

/**
 * TestContainers depends on construction of recursive generic types like class C<SELF extends C<SELF>>. It doesn't work
 * in kotlin. See:
 * https://youtrack.jetbrains.com/issue/KT-17186
 * https://github.com/testcontainers/testcontainers-java/issues/318
 * The class is a workaround for the problem.
 */
private class GenericContainerImpl(dockerImageName: String) : GenericContainer<GenericContainerImpl>(dockerImageName)