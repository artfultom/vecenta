package test.pack.client.v1;

import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class ClientNumberOne {
    private final Client client;

    public ClientNumberOne(Client client) {
        this.client = client;
    }

    public Integer method_name(my.artfultom.vecenta.controller.v1.A argument_name) {
        Request req = new Request(
                "entity_name.method_name(my.artfultom.vecenta.controller.v1.A)",
                List.of(ByteBuffer.allocate(4).putInt(a).array(), ByteBuffer.allocate(4).putInt(b).array())
        );
        try {
            Response resp = client.send(req);
            return ByteBuffer.wrap(resp.getResults().get(0)).getInt();
        } catch (ConnectException e) {
            e.printStackTrace();
            return null;
        }
    }
}