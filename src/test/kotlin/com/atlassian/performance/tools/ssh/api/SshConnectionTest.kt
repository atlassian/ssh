package com.atlassian.performance.tools.ssh.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant.now
import java.util.Collections
import java.util.concurrent.Executors

class SshConnectionTest {

    @Test
    fun shouldRunCommandOverSsh() {
        SshContainer().useConnection { ssh: SshConnection ->
            val sshResult = ssh.safeExecute("echo test")

            assertThat(sshResult.isSuccessful()).isTrue()
            assertThat(sshResult.output).isEqualTo("test\n")
        }
    }

    @Test
    fun shouldBeConcurrent() {
        // given
        val connectionConcurrency = 256
        val connectionDuration = Duration.ofSeconds(16)
        val commandsPerConnection = 4
        val commandDuration = connectionDuration.dividedBy(commandsPerConnection.toLong())

        // when
        val actualDuration = SshContainer().useSsh { ssh ->
            timeParallel(connectionConcurrency) {
                ssh.newConnection().use { connection ->
                    val command = "sleep ${commandDuration.seconds}"
                    val timeout = commandDuration + Duration.ofSeconds(2)
                    repeat(commandsPerConnection) { connection.execute(command, timeout) }
                }
            }
        }

        // then
        val acceptableOverhead = Duration.ofMillis(200).multipliedBy(connectionConcurrency.toLong())
        assertThat(actualDuration).isBetween(connectionDuration, connectionDuration + acceptableOverhead)
        println("actualDuration = $actualDuration")
    }

    private fun timeParallel(concurrency: Int, task: () -> Unit): Duration {
        return timeParallel(Collections.nCopies(concurrency, task))
    }

    private fun timeParallel(tasks: List<() -> Unit>): Duration {
        val start = now()
        val pool = Executors.newFixedThreadPool(tasks.size)
        tasks
            .map { pool.submit(it) }
            .forEach { it.get() }
        pool.shutdownNow()
        return Duration.between(start, now())
    }
}