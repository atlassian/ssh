package com.atlassian.performance.tools.ssh

import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection.SshResult
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * An [SshConnection] based on the [SSHJ library](https://github.com/hierynomus/sshj).
 */
internal class SshjConnection internal constructor(
    private val ssh: SSHClient,
    private val username: String
) : SshConnection {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun execute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult {
        val sshResult = safeExecute(
            cmd = cmd,
            timeout = timeout,
            stdout = stdout,
            stderr = stderr
        )
        if (!sshResult.isSuccessful()) {
            throw Exception("Error while executing $cmd. Exit status code $sshResult")
        }
        return sshResult
    }

    override fun safeExecute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult = ssh
        .startSession()
        .use { safeExecute(it, cmd, timeout, stdout, stderr) }

    private fun safeExecute(
        session: Session,
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult {
        logger.debug("$username$ $cmd")
        return session.exec(cmd).use { command ->
            command.waitForCompletion(cmd, timeout)
            SshResult(
                exitStatus = command.exitStatus,
                output = command.inputStream.readAndLog(stdout),
                errorOutput = command.errorStream.readAndLog(stderr)
            )
        }
    }

    override fun startProcess(cmd: String): DetachedProcess {
        return ssh.startSession().use { DetachedProcess.start(cmd, it) }
    }

    override fun stopProcess(process: DetachedProcess) {
        ssh.startSession().use { process.stop(it) }
    }

    override fun download(remoteSource: String, localDestination: Path) {
        localDestination.toFile().parentFile.ensureDirectory()
        val scpFileTransfer = ssh.newSCPFileTransfer()
        scpFileTransfer.download(remoteSource, localDestination.toString())
    }

    override fun upload(localSource: File, remoteDestination: String) {
        val scpFileTransfer = ssh.newSCPFileTransfer()
        scpFileTransfer.upload(localSource.absolutePath, remoteDestination)
    }

    private fun Session.Command.waitForCompletion(
        cmd: String,
        timeout: Duration
    ) {
        val expectedEnd = Instant.now().plus(timeout)
        val extendedTime = timeout.multipliedBy(5).dividedBy(4)
        try {
            this.join(extendedTime.toMillis(), TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            val output = readOutput(cmd)
            throw Exception("SSH command failed to finish in extended time ($extendedTime): $output", e)
        }
        val actualEnd = Instant.now()
        if (actualEnd.isAfter(expectedEnd)) {
            val overtime = Duration.between(expectedEnd, actualEnd)
            throw Exception("SSH command exceeded timeout $timeout by $overtime: '$cmd'")
        }
    }

    private fun Session.Command.readOutput(
        cmd: String
    ): SshExecutedCommand {
        return try {
            this.close()
            SshExecutedCommand(
                cmd = cmd,
                stdout = this.inputStream.reader().use { it.readText() },
                stderr = this.errorStream.reader().use { it.readText() }
            )
        } catch (e: Exception) {
            logger.error("Failed do close ssh channel. Can't get command output", e)
            SshExecutedCommand(
                cmd = cmd,
                stdout = "<couldn't get command stdout>",
                stderr = "<couldn't get command stderr>"
            )
        }
    }

    private fun InputStream.readAndLog(level: Level): String {
        val output = this.reader().use { it.readText() }
        if (output.isNotBlank()) {
            logger.log(level, output)
        }
        return output
    }

    override fun close() {
        ssh.close()
    }

    private data class SshExecutedCommand(
        val cmd: String,
        val stdout: String,
        val stderr: String
    )
}
