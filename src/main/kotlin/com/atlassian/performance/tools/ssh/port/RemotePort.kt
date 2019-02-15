package com.atlassian.performance.tools.ssh.port

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener
import java.net.InetSocketAddress

internal class RemotePort(
    private val remotePort: Int
) {

    internal fun forward(sshClient: SSHClient, localPort: Int): AutoCloseable {
        val remotePortForwarder = sshClient.remotePortForwarder
        val forward = remotePortForwarder.bind(
            RemotePortForwarder.Forward(remotePort),
            SocketForwardingConnectListener(InetSocketAddress("localhost", localPort))
        )
        return AutoCloseable {
            sshClient.remotePortForwarder.cancel(forward)
            sshClient.disconnect()
        }
    }

    internal companion object {
        internal fun create(port: Int): RemotePort {
            return RemotePort(port)
        }
    }
}