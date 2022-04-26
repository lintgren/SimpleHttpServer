package com.invidi.simplewebserver.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class ResponseRequestFilter implements RequestFilter {

  @Override
  public void run(OutputStream outputStream, InputStream inputStream, HttpObject output) {
    try (outputStream) {
      Writer out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
      out.write(output.responseHeader);
      out.flush();
      if (output.responseBody != null) {
        out.write(output.responseBody);
      } else if (output.responseFile != null) {
        outputStream.write(output.responseFile);
      }
      out.flush();
      outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

}
