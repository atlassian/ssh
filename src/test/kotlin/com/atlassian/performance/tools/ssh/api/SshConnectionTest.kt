package com.atlassian.performance.tools.ssh.api

import org.apache.logging.log4j.Level
import org.junit.Assert
import org.junit.Test
import java.lang.Exception
import java.time.Duration
import java.time.Instant

class SshConnectionTest {

    @Test
    fun shouldRunCommandOverSsh() {
        SshContainer().useConnection { ssh: SshConnection ->
            val sshResult = ssh.safeExecute("echo test")

            Assert.assertTrue(sshResult.isSuccessful())
            Assert.assertEquals(sshResult.output, "test\n")
        }
    }

    @Test
    fun shouldRespectTimeouts() {
        SshContainer().useConnection { ssh: SshConnection ->
            val start = Instant.now()
            try {
                ssh.execute("sleep 100", Duration.ofSeconds(1), Level.OFF, Level.OFF)
            }catch (e : Exception){
                //ignore
            }
            val executeDuration = Duration.between( start, Instant.now())
            Assert.assertTrue(executeDuration < Duration.ofSeconds(10))
        }
    }
}