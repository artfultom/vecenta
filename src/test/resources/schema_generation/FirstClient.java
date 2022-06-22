package test.pack.client.v1;

import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class FirstClient {
    private final Client client;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public FirstClient(Client client) {
        this.client = client;
    }

    public Integer method_name(Integer argument_name) throws ConnectException, ProtocolException {
        String name = "entity2.method_name(int32)->int32";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(Integer.class, argument_name));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(Integer.class, result);
    }
}