package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

import java.io.IOException;
import java.net.ConnectException;

public interface Connector extends AutoCloseable, Cloneable {

    void connect(String host, int port) throws ConnectException;

    Response send(Request request) throws ConnectException;

    @Override
    void close() throws IOException;
}