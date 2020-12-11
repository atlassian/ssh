package com.atlassian.performance.tools.ssh

import com.atlassian.performance.tools.ssh.api.SshConnection
import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class WaitingCommand(
    private val command: Session.Command,
    private val timeout: Duration,
    private val stdout: Level,
    private val stderr: Level
) {

    fun waitForResult(): SshConnection.SshResult {
        command.waitForCompletion(timeout)
        return SshConnection.SshResult(
            exitStatus = command.exitStatus,
            output = command.inputStream.readAndLog(stdout),
            errorOutput = command.errorStream.readAndLog(stderr)
        )
    }

    private fun Session.Command.waitForCompletion(
        timeout: Duration
    ) {
        val expectedEnd = Instant.now().plus(timeout)
        val extendedTime = timeout.multipliedBy(5).dividedBy(4)
        try {
            this.join(extendedTime.toMillis(), TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            val output = readOutput()
            throw Exception("SSH command failed to finish in extended time ($extendedTime): $output", e)
        }
        val actualEnd = Instant.now()
        if (actualEnd.isAfter(expectedEnd)) {
            val overtime = Duration.between(expectedEnd, actualEnd)
            throw Exception("SSH command exceeded timeout $timeout by $overtime")
        }
    }

    private fun Session.Command.readOutput(): SshjExecutedCommand {
        return try {
            this.close()
            SshjExecutedCommand(
                stdout = this.inputStream.reader().use { it.readText() },
                stderr = this.errorStream.reader().use { it.readText() }
            )
        } catch (e: Exception) {
            LOG.error("Failed do close ssh channel. Can't get command output", e)
            SshjExecutedCommand(
                stdout = "<couldn't get command stdout>",
                stderr = "<couldn't get command stderr>"
            )
        }
    }

    private fun InputStream.readAndLog(level: Level): String {
        val output = this.reader().use { it.readText() }
        if (output.isNotBlank()) {
            LOG.log(level, output)
        }
        return output
    }

    private data class SshjExecutedCommand(
        val stdout: String,
        val stderr: String
    )

    private companion object {
        private val LOG: Logger = LogManager.getLogger(this::class.java)
    }
}
