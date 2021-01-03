package my.artfultom.vecenta.controller;

import my.artfultom.vecenta.generate.DefaultCodeGenerateStrategy;
import my.artfultom.vecenta.generate.FileGenerator;
import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

public class ControllerTest {

    @Test
    public void testGenerate() throws IOException, URISyntaxException {
        URL schema = getClass().getResource("/schema");

        new FileGenerator(new DefaultCodeGenerateStrategy()).generateServerFiles(schema);
    }

    @Test
    public void test() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(ServerController.class); // TODO one or many?

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);

            client.startConnection("127.0.0.1", 5550);
            ClientConnector clientConnector = new ClientConnector(client);
            int result = clientConnector.sum(3, 4);

            Assert.assertEquals(5, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClientConnector {
        private Client client;

        public ClientConnector(Client client) {
            this.client = client;
        }

        // TODO must be generated
        public Integer sum(Integer a, int b) {
            Request req = new Request(
                    "ServerController.sum(java.lang.Integer,int)",
                    List.of(ByteBuffer.allocate(4).putInt(2).array(), ByteBuffer.allocate(4).putInt(3).array())
            );
            Response resp = null;

            try {
                resp = client.send(req);
            } catch (ConnectException e) {
                e.printStackTrace();
                return null;
            }

            return ByteBuffer.wrap(resp.getResults().get(0)).getInt();
        }
    }

    public static class ServerController {

        public Integer sum(Integer a, int b) {
            return a + b;
        }
    }
}