package com.invidi.simplewebserver.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.invidi.simplewebserver.context.WebServerContext;
import com.invidi.simplewebserver.context.SimpleWebServerContext;

public class SimpleWebServer implements WebServer {

   private ServerSocket server;
   private WebServerContext webServerContext;
   private Integer INTEGER_NUMBER_OF_REQUEST_THREADS = 20;

   public SimpleWebServer() {
      this.webServerContext = SimpleWebServerContext.getWebServerContext();
   }

   @Override
   public void start(int port) throws IOException {
      this.server = new ServerSocket(port);
      for (int i = 0; i < INTEGER_NUMBER_OF_REQUEST_THREADS; i++) {
         Thread t = new Thread(new RequestHandler());
         t.start();
      }
      System.out.println("PORT: " + server.getLocalPort());
      System.out.println("Doc root: " + webServerContext.getStaticPath());
      while (true) {
         Socket request = server.accept();
         RequestHandler.handleRequest(request);
      }
   }

   @Override
   public void stop() {
      try {
         server.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   @Override
   public WebServerContext getWebContext() {
      return this.webServerContext;
   }
}
