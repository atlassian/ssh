package com.atlassian.performance.tools.ssh.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SshConnectionTest {

    @Test
    fun shouldRunCommandOverSsh() {
        SshContainer().useConnection { ssh: SshConnection ->
            val sshResult = ssh.safeExecute("echo test")

            assertThat(sshResult.isSuccessful()).isTrue()
            assertThat(sshResult.output).isEqualTo("test\n")
        }
    }
}