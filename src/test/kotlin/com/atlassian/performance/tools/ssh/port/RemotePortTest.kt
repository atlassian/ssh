package com.atlassian.performance.tools.ssh.port

import com.atlassian.performance.tools.ssh.api.SshContainer
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.Executor
import kotlin.concurrent.thread

class RemotePortTest {

    @Test
    fun shouldForwardRemotePorts() {
        val localPort = 8866
        val remotePort = 8877
        val message = "hello"
        val server = HttpServer.create(InetSocketAddress(localPort), 0)
        server.executor = Executor { runnable ->
            thread(isDaemon = true) {
                runnable.run()
            }
        }
        server.createContext("/").handler = HttpHandler { exchange ->
            exchange.sendResponseHeaders(200, message.toByteArray().size.toLong())
            val outputStream = exchange.responseBody
            outputStream.write(message.toByteArray())
            outputStream.close()
        }
        server.start()

        SshContainer().useSsh { ssh ->
            ssh.forwardRemotePort(
                localPort = localPort,
                remotePort = remotePort
            ).use {
                val result = ssh
                    .newConnection()
                    .use {
                        it.execute("""wget -q -O - localhost:$remotePort/""")
                    }

                assertThat(result.isSuccessful()).isTrue()
                assertThat(result.output).isEqualTo(message)
            }

        }
        server.stop(0)
    }

}