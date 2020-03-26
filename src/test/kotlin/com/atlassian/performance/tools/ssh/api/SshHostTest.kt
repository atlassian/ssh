package com.atlassian.performance.tools.ssh.api

import com.atlassian.performance.tools.ssh.api.auth.PasswordAuthentication
import com.atlassian.performance.tools.ssh.api.auth.PublicKeyAuthentication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.file.Paths

class SshHostTest {

    @Test
    fun shouldSerializeToJsonWithPassword() {
        val sshHost = SshHost(
            "127.0.0.1",
            "name",
            PasswordAuthentication("password"),
            22
        )

        val sshHostFromJson = SshHost(sshHost.toJson())

        assertThat(sshHost).isEqualTo(sshHostFromJson)
    }

    @Test
    fun shouldSerializeToJsonWithKey() {
        val sshHost = SshHost(
            "127.0.0.1",
            "name",
            PublicKeyAuthentication(Paths.get("/public/key")),
            22
        )

        val sshHostFromJson = SshHost(sshHost.toJson())

        assertThat(sshHost).isEqualTo(sshHostFromJson)
    }
}