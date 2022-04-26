package com.invidi.simplewebserver.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invidi.simplewebserver.Exceptions.SimpleWebServerException;

public class ExceptionHandlerFilter extends RequestFilterDecorator {

  public ExceptionHandlerFilter(RequestFilter requestFilter) {
    super(requestFilter);
  }

  @Override
  public void run(OutputStream outputStream, InputStream inputStream, HttpObject input) {
    try {
      super.run(outputStream, inputStream, input);
    } catch (SimpleWebServerException simpleWebServerException) {
      try (Writer out = new OutputStreamWriter(outputStream)) {
        ObjectMapper om = new ObjectMapper();
        String exceptionObject = om.convertValue(simpleWebServerException, JsonNode.class).toString();
        String statusCodeHeader = String.format("HTTP/1.0 %s %s\r\n", simpleWebServerException.getStatusCode().value(),
            simpleWebServerException.getStatusCode().name());
        out.write(statusCodeHeader);
        Date now = new Date();
        out.write("Accept: " + "application/json");
        out.write("Date: " + now + "\r\n");
        out.write("Server: SimpleWeb/1.0" + "\r\n");
        out.write("Content-length: " + exceptionObject.length() + "\r\n");
        out.write("Content-type: " + "text/html" + "\r\n\r\n");
        out.flush();

        outputStream.write(exceptionObject.getBytes());
        outputStream.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }
}
