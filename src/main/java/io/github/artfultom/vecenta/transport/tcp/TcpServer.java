package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.AbstractServer;
import io.github.artfultom.vecenta.transport.MessageStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpServer extends AbstractServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private AsynchronousServerSocketChannel listener;

    private long timeout = Configuration.getLong("server.default_timeout");
    private static final long FIRST_CLIENT_ID = Configuration.getLong("server.first_client_id");

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(int port, ServerMatcher matcher) {
        try {
            listener = AsynchronousServerSocketChannel.open();
            listener.bind(new InetSocketAddress(port));

            listener.accept(FIRST_CLIENT_ID, new CompletionHandler<>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Long att) {
                    log.info(String.format("Client #%s was accepted", att));

                    if (listener.isOpen()) {
                        listener.accept(att + 1, this);
                    } else {
                        return;
                    }

                    try (MessageStream stream = new TcpMessageStream(ch, timeout)) {
                        if (ch.isOpen()) {
                            handshake(stream);
                        }

                        while (ch.isOpen()) {
                            byte[] req = stream.getMessage();
                            if (!listener.isOpen() || !ch.isOpen()) {
                                continue;
                            }

                            byte[] resp = matcher.process(req);

                            stream.sendMessage(resp);
                        }
                    } catch (IOException | ConnectionException e) {
                        log.error("Stream error", e);
                    }
                }

                @Override
                public void failed(Throwable e, Long att) {
                    log.warn(String.format("Client #%d. %s", att, e.getMessage()), e);
                }
            });
        } catch (IOException e) {
            log.error("Cannot open asynchronous server socket channel", e);
        }
    }

    @Override
    public void close() throws ConnectionException {
        try {
            if (listener.isOpen()) {
                listener.close();
            }
        } catch (IOException e) {
            throw new ConnectionException("Cannot close the server.", e);
        }
    }
}
