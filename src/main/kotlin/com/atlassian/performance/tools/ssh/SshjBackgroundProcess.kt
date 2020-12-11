package com.atlassian.performance.tools.ssh

import com.atlassian.performance.tools.ssh.api.BackgroundProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

internal class SshjBackgroundProcess(
    private val session: Session,
    private val command: Session.Command
) : BackgroundProcess {

    private var closed = AtomicBoolean(false)

    override fun stop(timeout: Duration): SshConnection.SshResult {
        tryToInterrupt()
        val result = WaitingCommand(command, timeout, Level.DEBUG, Level.DEBUG).waitForResult()
        close()
        return result
    }

    private fun tryToInterrupt() {
        try {
            sendSigint()
        } catch (e: Exception) {
            LOG.debug("cannot interrupt, if the command doesn't run anymore, then the write connection is closed", e)
        }
    }

    /**
     * [Session.Command.signal] doesn't work, so send the CTRL-C character rather than SSH-level SIGINT signal.
     * [OpenSSH server was not supporting this standard](https://bugzilla.mindrot.org/show_bug.cgi?id=1424).
     * It's supported since 7.9p1 (late 2018), but our test Ubuntu still runs on 7.6p1.
     */
    private fun sendSigint() {
        val ctrlC = 3
        command.outputStream.write(ctrlC);
        command.outputStream.flush();
    }

    override fun close() {
        if (!closed.getAndSet(true)) {
            command.use {}
            session.use {}
        }
    }

    private companion object {
        private val LOG = LogManager.getLogger(this::class.java)
    }
}
