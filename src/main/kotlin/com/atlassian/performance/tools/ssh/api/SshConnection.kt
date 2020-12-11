package com.atlassian.performance.tools.ssh.api

import org.apache.logging.log4j.Level
import java.io.Closeable
import java.io.File
import java.nio.file.Path
import java.time.Duration

/**
 * A secure shell connected with a remote server, which can execute commands and transfer files.
 *
 * @see [Ssh.newConnection]
 */
interface SshConnection : Closeable {

    /**
     * Executes [cmd]. Fails if the exit status is non-zero.
     * Times out after 30 seconds.
     * Logs standard output at the DEBUG level.
     * Logs standard errors at the WARN level.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     */
    @JvmDefault
    fun execute(
        cmd: String
    ): SshResult = execute(
        cmd = cmd,
        timeout = Duration.ofSeconds(30),
        stdout = Level.DEBUG,
        stderr = Level.WARN
    )

    /**
     * Executes [cmd]. Fails if the exit status is non-zero.
     * Logs standard output at the DEBUG level.
     * Logs standard errors at the WARN level.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     * @param [timeout] Limits the amount of time spent on waiting for [cmd] to finish.
     */
    @JvmDefault
    fun execute(
        cmd: String,
        timeout: Duration
    ): SshResult = execute(
        cmd = cmd,
        timeout = timeout,
        stdout = Level.DEBUG,
        stderr = Level.WARN
    )

    /**
     * Executes [cmd]. Fails if the exit status is non-zero.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     * @param [timeout] Limits the amount of time spent on waiting for [cmd] to finish.
     * @param [stdout] Controls the log level of [cmd]'s standard output stream.
     * @param [stderr] Controls the log level of [cmd]'s standard error stream.
     */
    fun execute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult

    /**
     * Executes [cmd]. Returns the result regardless of the exit status.
     * Times out after 30 seconds.
     * Logs standard output at the TRACE level.
     * Logs standard errors at the DEBUG level.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     */
    @JvmDefault
    fun safeExecute(
        cmd: String
    ): SshResult = safeExecute(
        cmd = cmd,
        timeout = Duration.ofSeconds(30),
        stdout = Level.TRACE,
        stderr = Level.DEBUG
    )

    /**
     * Executes [cmd]. Returns the result regardless of the exit status.
     * Logs standard output at the TRACE level.
     * Logs standard errors at the DEBUG level.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     * @param [timeout] Limits the amount of time spent on waiting for [cmd] to finish.
     */
    @JvmDefault
    fun safeExecute(
        cmd: String,
        timeout: Duration
    ): SshResult = safeExecute(
        cmd = cmd,
        timeout = timeout,
        stdout = Level.TRACE,
        stderr = Level.DEBUG
    )

    /**
     * Executes [cmd]. Returns the result regardless of the exit status.
     *
     * @param [cmd] Runs within the secure shell on the remote system. For example: `pwd`.
     * @param [timeout] Limits the amount of time spent on waiting for [cmd] to finish.
     * @param [stdout] Controls the log level of [cmd]'s standard output stream.
     * @param [stderr] Controls the log level of [cmd]'s standard error stream.
     */
    fun safeExecute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult

    /**
     * Starts a [DetachedProcess]. You can use [stopProcess] to stop it later.
     */
    @Deprecated(message = "Use Ssh.runInBackground instead")
    fun startProcess(
        cmd: String
    ): DetachedProcess

    /**
     * Stops a [DetachedProcess].
     */
    @Deprecated(message = "Use BackgroundProcess.stop instead")
    fun stopProcess(
        process: DetachedProcess
    )

    /**
     * Downloads files from a remote system.
     *
     * @param remoteSource Points to the file on the remote machine.
     * @param localDestination Points to a destination on a local system.
     */
    fun download(
        remoteSource: String,
        localDestination: Path
    )

    /**
     * Uploads files to a remote system.
     *
     * @param localSource Points to the file on the local machine.
     * @param remoteDestination Points to a destination on a remote machine.
     */
    fun upload(
        localSource: File,
        remoteDestination: String
    )

    /**
     * @since 2.3.0
     */
    @JvmDefault
    fun getHost(): SshHost = throw Exception("Not implemented")

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
}
