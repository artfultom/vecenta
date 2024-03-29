package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.transport.tcp.TcpConnector;
import io.github.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class TransportTest {

    @Test
    public void manyClients() throws ConnectionException {
        int port = 5550;

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        try (Server server = new TcpServer()) {
            server.start(port, matcher);

            IntStream.range(0, 100).parallel()
                    .mapToObj(item -> CompletableFuture.runAsync(() -> {
                        try (Connector connector = new TcpConnector()) {
                            connector.connect("localhost", port);

                            for (int j = 0; j < 100; j++) {
                                byte[] param = ("param" + j).getBytes();
                                Response resp = connector.send(new Request("echo", List.of(param)));

                                Assert.assertNotNull(resp.getResult());
                                Assert.assertArrayEquals(param, resp.getResult());
                            }
                        } catch (ConnectionException e) {
                            Assert.fail(e.getMessage());
                        }
                    })).forEach(CompletableFuture::join);
        }
    }

    @Test
    public void errorConnectionRefused() throws ConnectionException {
        int port = 5551;

        for (int i = 0; i < 10; i++) {
            try (Connector connector = new TcpConnector()) {
                Assert.assertThrows(
                        Exception.class,
                        () -> connector.connect("localhost", port)
                );
            }
        }
    }

    @Test
    public void errorServerClosed() throws ConnectionException {
        int port = 5553;

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer(); Connector connector = new TcpConnector()) {
            server.start(port, matcher);
            connector.connect("localhost", port);

            byte[] val = TypeConverter.INTEGER.convert(42);
            Response resp = connector.send(new Request("echo", List.of(val)));
            Assert.assertArrayEquals(val, resp.getResult());

            server.close();

            Assert.assertThrows(
                    ConnectionException.class,
                    () -> connector.send(new Request("echo", List.of(val)))
            );
        }
    }

    @Test
    public void error1Handler() throws ConnectionException {
        int port = 5554;

        ServerMatcher matcher = new ServerMatcher();

        try (TcpServer server = new TcpServer(); Connector connector = new TcpConnector()) {
            server.start(port, matcher);
            connector.connect("localhost", port);

            Response response = connector.send(new Request("echo", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getErrorType());
            Assert.assertNull(response.getResult());
        }
    }

    @Test
    public void error2Handler() throws ConnectionException {
        int port = 5555;

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer(); Connector connector = new TcpConnector()) {
            server.start(port, matcher);
            connector.connect("localhost", port);

            Response response = connector.send(new Request("wrong", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getErrorType());
            Assert.assertNull(response.getResult());
        }
    }

    @Test
    public void manyResults() throws ConnectionException {
        int port = 5556;

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("inc", request -> {
            int val = ByteBuffer.wrap(request.getParams().get(0)).getInt();
            val++;

            return new Response(ByteBuffer.allocate(Integer.BYTES).putInt(val).array());
        }));

        try (
                TcpServer server = new TcpServer();
                Connector connector1 = new TcpConnector();
                Connector connector2 = new TcpConnector()
        ) {
            server.start(port, matcher);
            connector1.connect("localhost", port);
            connector2.connect("localhost", port);

            IntStream.range(0, 1000).parallel()
                    .mapToObj(item -> CompletableFuture.runAsync(() -> {
                        try {
                            int val = new Random().nextInt();
                            Response response1 = connector1.send(new Request(
                                    "inc",
                                    List.of(ByteBuffer.allocate(Integer.BYTES).putInt(val).array())
                            ));

                            Assert.assertNotNull(response1);
                            Assert.assertNull(response1.getErrorType());
                            Assert.assertNotNull(response1.getResult());
                            Assert.assertEquals(val + 1, ByteBuffer.wrap(response1.getResult()).getInt());

                            Response response2 = connector2.send(new Request(
                                    "inc",
                                    List.of(ByteBuffer.allocate(Integer.BYTES).putInt(val).array())
                            ));

                            Assert.assertNotNull(response2);
                            Assert.assertNull(response2.getErrorType());
                            Assert.assertNotNull(response2.getResult());
                            Assert.assertEquals(val + 1, ByteBuffer.wrap(response2.getResult()).getInt());
                        } catch (ConnectionException e) {
                            Assert.fail(e.getMessage());
                        }
                    })).forEach(CompletableFuture::join);
        }
    }
}