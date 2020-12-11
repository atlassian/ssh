package com.atlassian.performance.tools.ssh.api

import com.atlassian.performance.tools.jvmtasks.api.ExponentialBackoff
import com.atlassian.performance.tools.jvmtasks.api.IdempotentAction
import com.atlassian.performance.tools.ssh.PerformanceDefaultConfig
import com.atlassian.performance.tools.ssh.SshjBackgroundProcess
import com.atlassian.performance.tools.ssh.SshjConnection
import com.atlassian.performance.tools.ssh.port.LocalPort
import com.atlassian.performance.tools.ssh.port.RemotePort
import net.schmizz.sshj.SSHClient
import java.time.Duration

/**
 * Connects to [host] via SSH.
 *
 * @param host remote SSH server we're connecting to.
 * @param connectivityPatience how many times we're going to try to connect to the server.
 */
data class Ssh(
    val host: SshHost,
    private val connectivityPatience: Int
) {

    /**
     * Connects to [host] via SSH with up to 4 retries.
     *
     * @param host remote SSH server we're connecting to.
     */
    constructor(
        host: SshHost
    ) : this(
        host = host,
        connectivityPatience = 4
    )

    /**
     * Connects to [host].
     *
     * @return A new [SshConnection].
     */
    fun newConnection(): SshConnection {
        return SshjConnection(
            prepareClient(),
            host
        )
    }

    /**
     * Runs [cmd] in the background, without waiting for its completion. The returned process can be stopped later.
     *
     * @since 2.4.0
     */
    fun runInBackground(cmd: String): BackgroundProcess {
        val session = prepareClient().startSession()
        session.allocateDefaultPTY()
        val command = session.exec(cmd)
        return SshjBackgroundProcess(session, command)
    }

    /**
     * Creates an encrypted connection between a local machine and a remote machine through which you can relay traffic.
     *
     * See https://www.ssh.com/ssh/tunneling/example#sec-What-Is-SSH-Port-Forwarding-aka-SSH-Tunneling.
     *
     * Listen for connections on local machine and [localPort].
     * Forwards all the traffic to a remote machine and [remotePort].
     *
     * @param localPort port on the local host.
     * @param remotePort localPort on a remote machine.
     * @since 2.2.0
     */
    fun forwardLocalPort(localPort: Int, remotePort: Int): AutoCloseable {
        return LocalPort.create(localPort).forward(prepareClient(), remotePort)
    }

    /**
     * Creates an encrypted connection between a local machine and a remote machine through which you can relay traffic.
     *
     * See https://www.ssh.com/ssh/tunneling/example#sec-What-Is-SSH-Port-Forwarding-aka-SSH-Tunneling.
     *
     * Listen for connections on remote machine and [remotePort].
     * Forwards all the traffic to a local machine and [localPort].
     *
     * @param localPort port on the local host.
     * @param remotePort port on a remote machine.
     * @since 2.2.0
     */
    fun forwardRemotePort(localPort: Int, remotePort: Int): AutoCloseable {
        return RemotePort.create(remotePort).forward(prepareClient(), localPort)
    }

    private fun prepareClient(): SSHClient {
        val ssh = SSHClient(PerformanceDefaultConfig())
        ssh.connection.keepAlive.keepAliveInterval = 60
        ssh.addHostKeyVerifier { _, _, _ -> true }
        waitForConnectivity(ssh)
        host.authentication.authenticate(host.userName, ssh)
        return ssh
    }

    private fun waitForConnectivity(
        ssh: SSHClient
    ) {
        val address = host.ipAddress
        val port = host.port
        IdempotentAction("connect to $address on port $port") {
            ssh.connect(
                address,
                port
            )
        }.retry(
            maxAttempts = connectivityPatience,
            backoff = ExponentialBackoff(
                baseBackoff = Duration.ofSeconds(1)
            )
        )
    }
}
