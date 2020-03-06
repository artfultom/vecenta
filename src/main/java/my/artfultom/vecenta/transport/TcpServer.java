package my.artfultom.vecenta.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TcpServer implements Server {
    private AsynchronousServerSocketChannel listener = null;
    private Map<String, MethodHandler> handlerMap = new HashMap<>();

    private long timeout = 5000;

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(int port) {
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

                    // TODO handshake

                    MessageStream stream = new MessageStream(ch, timeout);

                    while (listener.isOpen()) {
                        byte[] req = stream.getNextMessage();
                        if (req == null) {
                            break;
                        }

                        byte[] resp = process(req);

                        stream.sendMessage(resp);
                    }
                }

                @Override
                public void failed(Throwable e, Void att) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean register(MethodHandler handler) {
        if (handlerMap.containsKey(handler.getName())) {
            return false;
        }

        handlerMap.put(handler.getName(), handler);
        return true;
    }

    private byte[] process(byte[] in) {
        ByteBuffer buf = ByteBuffer.wrap(in);

        int methodSize = buf.getInt(0);

        byte[] rawMethod = Arrays.copyOfRange(in, 4, methodSize + 4);
        String method = new String(rawMethod, StandardCharsets.UTF_8);

        List<byte[]> params = new ArrayList<>();
        for (int i = methodSize + 4; i < buf.capacity(); ) {
            byte[] rawSize = Arrays.copyOfRange(in, i, i + 4);
            int paramSize = ByteBuffer.wrap(rawSize).getInt();

            byte[] param = Arrays.copyOfRange(in, i + 4, paramSize + i + 4);

            params.add(param);

            i += paramSize + 4;
        }

        MethodHandler handler = handlerMap.get(method);
        if (handler == null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataStream = new DataOutputStream(out);

            try {
                dataStream.writeByte(1);
                dataStream.writeInt(666); // TODO code
            } catch (IOException e) {
                e.printStackTrace();
            }

            return out.toByteArray();
        }

        Response response = handler.execute(new Request(method, params));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(out);

        try {
            dataStream.writeByte(0);
            for (byte[] param : response.getParams()) {
                dataStream.writeInt(param.length);
                dataStream.write(param);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    @Override
    public void stop() {
        try {
            if (listener.isOpen()) {
                listener.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}