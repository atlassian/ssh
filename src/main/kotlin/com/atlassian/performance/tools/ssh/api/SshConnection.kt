package com.atlassian.performance.tools.ssh.api

import com.atlassian.performance.tools.io.api.ensureDirectory
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.Closeable
import java.io.InputStream
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * [SshConnection] executes remote commands and download files from a remote server.
 *
 * @see [Ssh.newConnection]
 */
class SshConnection internal constructor(
    private val ssh: SSHClient,
    private val username: String
) : Closeable {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    override fun close() {
        ssh.close()
    }

    /**
     * Executes a remote command. Throws an exception if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish. Defaults to 30 seconds.
     * @param stdout Controls the log level of [cmd]'s standard output stream. Defaults to [Level.DEBUG].
     * @param stderr Controls the log level of [cmd]'s standard error stream. Defaults to [Level.WARN].
     */
    @JvmOverloads
    fun execute(
        cmd: String,
        timeout: Duration = Duration.ofSeconds(30),
        stdout: Level = Level.DEBUG,
        stderr: Level = Level.WARN
    ): SshResult {
        val sshResult = safeExecute(
            cmd = cmd,
            timeout = timeout,
            stdout = stdout,
            stderr = stderr
        )
        if (!sshResult.isSuccessful()) {
            throw RuntimeException("Error while executing $cmd. Exit status code $sshResult")
        }
        return sshResult
    }

    /**
     * Executes remote command.
     * The command is very similar to [execute], but it will return [SshResult] if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish. Defaults to 30 seconds.
     * @param stdout Controls the log level of [cmd]'s standard output stream. Defaults to [Level.DEBUG].
     * @param stderr Controls the log level of [cmd]'s standard error stream. Defaults to [Level.WARN].
     */
    @JvmOverloads
    fun safeExecute(
        cmd: String,
        timeout: Duration = Duration.ofSeconds(30),
        stdout: Level = Level.TRACE,
        stderr: Level = Level.DEBUG
    ): SshResult {
        ssh.startSession()
            .use { session ->
                logger.debug("$username$ $cmd")
                session.exec(cmd).use { command ->
                    command.waitForCompletion(cmd, timeout)
                    return SshResult(
                        exitStatus = command.exitStatus,
                        output = command.inputStream.readAndLog(stdout),
                        errorOutput = command.errorStream.readAndLog(stderr)
                    )
                }
            }
    }

    /**
     * Starts a [DetachedProcess]. You can use [stopProcess] to stop it later.
     */
    fun startProcess(cmd: String): DetachedProcess {
        return ssh.startSession().use { DetachedProcess.start(cmd, it) }
    }

    /**
     * Stops a [DetachedProcess].
     */
    fun stopProcess(process: DetachedProcess) {
        ssh.startSession().use { process.stop(it) }
    }

    /**
     * Downloads files from a remote system.
     *
     * @param remoteSource Points to the file on the remote machine.
     * @param localDestination Points to a destination on a local system.
     */
    fun download(remoteSource: String, localDestination: Path) {
        localDestination.toFile().parentFile.ensureDirectory()
        val scpFileTransfer = ssh.newSCPFileTransfer()
        scpFileTransfer.download(remoteSource, localDestination.toString())
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

    /**
     * Holds results of a SSH command.
     *
     * @param exitStatus Holds exit code from a remotely executed SSH command.
     * @param output Holds standard output produced by a SSH command.
     * @param errorOutput Holds standard error produced by a SSH command.
     */
    data class SshResult(
        val exitStatus: Int,
        val output: String,
        val errorOutput: String
    ) {
        fun isSuccessful(): Boolean {
            return exitStatus == 0
        }
    }

    private data class SshExecutedCommand(
        val cmd: String,
        val stdout: String,
        val stderr: String
    )
}
