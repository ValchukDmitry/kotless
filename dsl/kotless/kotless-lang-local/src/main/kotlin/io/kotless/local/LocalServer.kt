package io.kotless.local

import io.kotless.dsl.LambdaHandler
import io.kotless.local.handler.DynamicHandler
import io.kotless.local.handler.StaticHandler
import io.kotless.local.scheduled.Scheduler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList


internal class LocalServer(port: Int) {
    private val handler = LambdaHandler()

    private val server: Server = Server(port)
    private val scheduler: Scheduler = Scheduler(handler)

    init {
        server.handler = HandlerList(StaticHandler(), DynamicHandler(handler))
        server.stopAtShutdown = true
    }

    fun start() {
        server.start()
        scheduler.start()
    }

    fun join() {
        server.join()
    }

    fun stop() {
        scheduler.stop()
        server.stop()
    }
}
