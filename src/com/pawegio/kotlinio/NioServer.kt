package com.pawegio.kotlinio

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

/**
 * @author Crunchify
 * @author pawegio
 */
@Throws(IOException::class)
fun main(args: Array<String>) {
    val selector = Selector.open()

    val socket = ServerSocketChannel.open()
    val address = InetSocketAddress("localhost", 1111)
    socket.apply {
        bind(address)
        configureBlocking(false)
        register(selector, socket.validOps(), null)
    }

    while (true) {
        log("Waiting")
        selector.select()

        val keys = selector.selectedKeys()
        val iterator = keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (key.isAcceptable) {
                socket.accept().apply {
                    configureBlocking(false)
                    register(selector, SelectionKey.OP_READ)
                    log("Connection accepted: $localAddress")
                }
            } else if (key.isReadable) {
                val client = key.channel() as SocketChannel
                val buffer = ByteBuffer.allocate(256)
                client.read(buffer)
                val result = String(buffer.array()).trim('\u0000')
                log("Message received: $result")

                if (result == "Close") {
                    client.close()
                    log("Connection closed")
                }
            }
            iterator.remove()
        }
    }
}