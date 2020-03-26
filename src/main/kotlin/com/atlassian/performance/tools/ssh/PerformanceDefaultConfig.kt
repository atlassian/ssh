package com.atlassian.performance.tools.ssh

import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.transport.random.BouncyCastleRandom
import net.schmizz.sshj.transport.random.JCERandom
import net.schmizz.sshj.transport.random.Random
import net.schmizz.sshj.transport.random.SingletonRandomFactory
import net.schmizz.sshj.common.Factory

internal class PerformanceDefaultConfig : DefaultConfig() {
    companion object {
        val bcFactory = MemoizingFactory(BouncyCastleRandom.Factory())
        val jceFactory = MemoizingFactory(JCERandom.Factory())
    }
    override fun initRandomFactory(bouncyCastleRegistered: Boolean) {
        randomFactory = SingletonRandomFactory(if (bouncyCastleRegistered) bcFactory else jceFactory)
    }

    class MemoizingFactory(private val factory: Factory<Random>) : Factory<Random> {
        val random : Random by lazy { factory.create() }
        override fun create(): Random {
            return random
        }
   }
}