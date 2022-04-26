package com.invidi.simplewebserver.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.invidi.simplewebserver.context.WebServerContext;
import com.invidi.simplewebserver.context.SimpleWebServerContext;
import com.invidi.simplewebserver.filters.ExceptionHandlerFilter;
import com.invidi.simplewebserver.filters.HeaderFilter;
import com.invidi.simplewebserver.filters.HttpObject;
import com.invidi.simplewebserver.filters.ReourceMapperFilter;
import com.invidi.simplewebserver.filters.RequestFilter;
import com.invidi.simplewebserver.filters.ResponseRequestFilter;

public class RequestHandler implements Runnable {

  private static List<Socket> pool = new LinkedList<Socket>();
  private WebServerContext webServerContext;
  private RequestFilter filters;

  public RequestHandler() {
    this.webServerContext = SimpleWebServerContext.getWebServerContext();
    filters = new ExceptionHandlerFilter(new HeaderFilter(
        new ReourceMapperFilter(new ResponseRequestFilter(), webServerContext)));
  }

  @Override
  public void run() {
    while (true) {
      Socket connection = null;
      synchronized (pool) {
        while (pool.isEmpty()) {
          try {
            pool.wait();
          } catch (InterruptedException ex) {

          }
        }
        connection = (Socket) pool.remove(0);
      }
      try (OutputStream outputStream = connection.getOutputStream();
          InputStream inputStream = connection.getInputStream()) {
        try {
          filters.run(outputStream, inputStream, new HttpObject());
          connection.close();
        } catch (Exception e) {
          e.printStackTrace();
          Writer out = new OutputStreamWriter(outputStream);
          out.write("HTTP/1.0 500 ERROR\r\n");
          Date now = new Date();
          out.write("Date: " + now + "\r\n");
          out.write("Server: SimpleWeb/1.0" + "\r\n");
          out.write("Content-length: 0" + "\r\n");
          out.write("Content-type: " + "text/html" + "\r\n\r\n");
          out.flush();
          outputStream.flush();
        }
      } catch (IOException e1) {
        e1.printStackTrace();
      }

    }

  }

  public static void handleRequest(Socket request) {
    synchronized (pool) {
      pool.add(pool.size(), request);
      pool.notifyAll();
    }
  }

}
