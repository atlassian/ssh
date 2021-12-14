package com.atlassian.performance.tools.ssh

import com.atlassian.performance.tools.io.api.ensureDirectory
import com.atlassian.performance.tools.ssh.api.DetachedProcess
import com.atlassian.performance.tools.ssh.api.SshConnection
import com.atlassian.performance.tools.ssh.api.SshConnection.SshResult
import com.atlassian.performance.tools.ssh.api.SshHost
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Path
import java.time.Duration

/**
 * An [SshConnection] based on the [SSHJ library](https://github.com/hierynomus/sshj).
 */
internal class SshjConnection internal constructor(
    private val ssh: SSHClient,
    private val sshHost: SshHost
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
        logger.debug("${sshHost.userName}@${sshHost.ipAddress}$ $cmd")
        return session.exec(cmd).use { command ->
            WaitingCommand(command, timeout, stdout, stderr).waitForResult()
        }
    }

    @Suppress("DEPRECATION", "OverridingDeprecatedMember") // used in public API, can only remove in a MAJOR release
    override fun startProcess(cmd: String): DetachedProcess {
        return ssh.startSession().use { DetachedProcess.start(cmd, it) }
    }

    @Suppress("DEPRECATION", "OverridingDeprecatedMember") // used in public API, can only remove in a MAJOR release
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

    override fun getHost(): SshHost = sshHost

    override fun close() {
        ssh.close()
    }
}
