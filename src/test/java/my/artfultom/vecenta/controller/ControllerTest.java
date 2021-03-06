package my.artfultom.vecenta.controller;

import my.artfultom.vecenta.generate.CodeGenerateStrategy;
import my.artfultom.vecenta.generate.Configuration;
import my.artfultom.vecenta.generate.DefaultCodeGenerateStrategy;
import my.artfultom.vecenta.generate.FileGenerator;
import my.artfultom.vecenta.generated.v1.SumClient;
import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ControllerTest {

    @Test
    public void testGeneration() throws IOException, URISyntaxException {
        Path schemaDir = Path.of(getClass().getResource("/schema_generation").toURI());
        Path tempDir = Files.createTempDirectory("test_" + System.currentTimeMillis());

        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();
        Configuration config = new Configuration(
                schemaDir,
                tempDir,
                "test.pack.server",
                "test.pack.client"
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);

        for (Path file : files) {
            String expectedFileName = file.getFileName().toString();
            Path expected = Path.of(
                    getClass().getResource("/schema_generation/" + expectedFileName).toURI()
            );

            assertEquals(Files.readString(expected), Files.readString(file));

            Files.delete(file);
        }
    }

    @Test
    public void testController() throws URISyntaxException, IOException, ClassNotFoundException {
        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();
        Configuration config = new Configuration(
                Path.of(getClass().getResource("/schema_controller").toURI()),
                Path.of("src", "test", "java"),
                "my.artfultom.vecenta.generated",
                "my.artfultom.vecenta.generated"
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);

        ServerMatcher matcher = new ServerMatcher();

        // TODO find
        ClassLoader cl = new URLClassLoader(new URL[]{files.get(0).getParent().toUri().toURL()});
        Class<?> serverClass = cl.loadClass("my.artfultom.vecenta.generated.v1.SumServerImpl");
        Class<?> clientClass = cl.loadClass("my.artfultom.vecenta.generated.v1.SumClient");
        matcher.register(serverClass);

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);

            client.startConnection("127.0.0.1", 5550);
            SumClient clientConnector = new SumClient(client);
            int result = clientConnector.sum(3, 2);

            Assert.assertEquals(5, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}