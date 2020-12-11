package com.atlassian.performance.tools.ssh.api

import java.time.Duration

/**
 * Runs in the background. Is independent of `SshConnection`s being closed.
 * Can be used for commands, which will not stop on their own, e.g. `tail -f`, `ping`, `top`, etc.
 * @since 2.4.0
 */
interface BackgroundProcess : AutoCloseable {

    /**
     * Interrupts the process, then waits up to [timeout] for its completion.
     * Skips the interrupt if the process is already finished.
     * Throws if getting the [SshConnection.SshResult] fails.
     * Closes the open resources.
     *
     * @return the result of the stopped process, could have a non-zero exit code
     */
    fun stop(timeout: Duration): SshConnection.SshResult
}
