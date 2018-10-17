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
     * Executes a remote command. Throws an exception if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     */
    fun execute(
        cmd: String
    ): SshResult

    /**
     * Executes a remote command. Throws an exception if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish.
     */
    fun execute(
        cmd: String,
        timeout: Duration
    ): SshResult

    /**
     * Executes a remote command. Throws an exception if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish.
     * @param stdout Controls the log level of [cmd]'s standard output stream.
     */
    fun execute(
        cmd: String,
        timeout: Duration,
        stdout: Level
    ): SshResult

    /**
     * Executes a remote command. Throws an exception if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish.
     * @param stdout Controls the log level of [cmd]'s standard output stream.
     * @param stderr Controls the log level of [cmd]'s standard error stream.
     */
    fun execute(
        cmd: String,
        timeout: Duration,
        stdout: Level,
        stderr: Level
    ): SshResult

    /**
     * Executes remote command.
     * The command is very similar to [execute], but it will return [SshResult] if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     */
    fun safeExecute(
        cmd: String
    ): SshResult

    /**
     * Executes remote command.
     * The command is very similar to [execute], but it will return [SshResult] if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish
     */
    fun safeExecute(
        cmd: String,
        timeout: Duration
    ): SshResult

    /**
     * Executes remote command.
     * The command is very similar to [execute], but it will return [SshResult] if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish
     * @param stdout Controls the log level of [cmd]'s standard output stream.
     */
    fun safeExecute(
        cmd: String,
        timeout: Duration,
        stdout: Level
    ): SshResult

    /**
     * Executes remote command.
     * The command is very similar to [execute], but it will return [SshResult] if [cmd] exits with a non-zero code.
     *
     * @param cmd Runs within the secure shell on the remote system. For example: `pwd`.
     * @param timeout Limits the amount of time spent on waiting for [cmd] to finish
     * @param stdout Controls the log level of [cmd]'s standard output stream.
     * @param stderr Controls the log level of [cmd]'s standard error stream.
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
    fun startProcess(
        cmd: String
    ): DetachedProcess

    /**
     * Stops a [DetachedProcess].
     */
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
