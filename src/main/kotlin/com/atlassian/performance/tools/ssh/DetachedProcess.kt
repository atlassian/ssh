package com.atlassian.performance.tools.ssh

import net.schmizz.sshj.connection.channel.direct.Session
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

class DetachedProcess private constructor(
    private val cmd: String,
    private val uuid: UUID
) {
    private val logger: Logger = LogManager.getLogger(this::class.java)

    companion object {
        private val logger: Logger = LogManager.getLogger(this::class.java)
        private val dir = "~/.jpt-processes"

        fun start(cmd: String, session: Session): DetachedProcess {
            val uuid = UUID.randomUUID()
            logger.debug("Starting process $uuid $cmd")
            session.exec("screen -dm bash -c '${savePID(uuid)} && $cmd'")
                .use { command -> command.join(15, TimeUnit.SECONDS) }
            return DetachedProcess(cmd, uuid)
        }

        private fun savePID(uuid: UUID): String = "mkdir -p $dir && echo $$ > $dir/$uuid"
    }

    fun stop(session: Session) {
        logger.debug("Stopping process $uuid $cmd")
        session.exec("kill -3 `cat $dir/$uuid`")
            .use { command -> command.join(15, TimeUnit.SECONDS) }
    }
}