package com.atlassian.performance.tools.ssh.api

import org.junit.Assert
import org.junit.Test

class SshConnectionTest {

    @Test
    fun shouldRunCommandOverSsh() {
        SshContainer().useConnection { ssh: SshConnection ->
            val sshResult = ssh.safeExecute("echo test")

            Assert.assertTrue(sshResult.isSuccessful())
            Assert.assertEquals(sshResult.output, "test\n")
        }
    }
}