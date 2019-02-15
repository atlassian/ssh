package com.atlassian.performance.tools.ssh.port

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.concurrent.thread

internal class LocalPort(
    private val localPort: Int
) {

    internal fun forward(sshClient: SSHClient, remotePort: Int): AutoCloseable {
        val params = LocalPortForwarder.Parameters("localhost", localPort, sshClient.remoteHostname, remotePort)
        val serverSocket = ServerSocket()
        serverSocket.reuseAddress = true
        serverSocket.bind(InetSocketAddress(params.localHost, params.localPort))
        val localPortForwarder = sshClient.newLocalPortForwarder(params, serverSocket)
        thread(
            isDaemon = true,
            name = "forwarding-local-localPort-$localPort-to-remote-localPort-$remotePort"
        ) {
            localPortForwarder.listen()
        }
        return AutoCloseable {
            serverSocket.close()
            localPortForwarder.close()
            sshClient.disconnect()
        }
    }

    internal companion object {
        internal fun create(port: Int): LocalPort {
            return LocalPort(port)
        }
    }
}