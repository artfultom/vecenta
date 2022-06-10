package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.AbstractServer;
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

    private long timeout = Configuration.getInt("server.default_timeout");

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(int port, ServerMatcher matcher) {
        try {
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att) {
                    if (listener.isOpen()) {
                        listener.accept(null, this);
                    } else {
                        return;
                    }

                    try (TcpMessageStream stream = new TcpMessageStream(ch, timeout)) {
                        if (listener.isOpen()) {
                            handshake(stream);
                        }

                        while (listener.isOpen()) {
                            byte[] req = stream.getNextMessage();
                            if (req == null || req.length == 0 || !listener.isOpen()) {
                                break;
                            }

                            byte[] resp = matcher.process(req);

                            stream.sendMessage(resp);
                        }
                    } catch (IOException e) {
                        log.error("Stream error", e);
                    }
                }

                @Override
                public void failed(Throwable e, Void att) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error("Cannot open asynchronous server socket channel", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (listener.isOpen()) {
            listener.close();
        }
    }
}
