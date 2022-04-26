package com.invidi.simplewebserver.main;

import java.io.IOException;

import com.invidi.simplewebserver.context.WebServerContext;

public interface WebServer {

    void start(int port) throws IOException;

    void stop();

    WebServerContext getWebContext();
}
