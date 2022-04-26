package com.invidi.simplewebserver.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.StringTokenizer;

import com.invidi.simplewebserver.model.RequestMethod;

public class HeaderFilter extends RequestFilterDecorator {

  public HeaderFilter(RequestFilter requestFilter) {
    super(requestFilter);
  }

  @Override
  public void run(OutputStream outputStream, InputStream inputStream, HttpObject input) {
    if (input == null) {
      input = new HttpObject();
    }
    try {
      Reader in = new InputStreamReader(new BufferedInputStream(inputStream));
      StringBuffer requestLine = new StringBuffer();
      int c;
      while (true) {
        c = in.read();
        if (c == '\r' || c == '\n' || c == -1)
          break;
        requestLine.append((char) c);
      }
      String get = requestLine.toString();
      StringTokenizer st = new StringTokenizer(get);
      String method = st.nextToken();
      input.requestMethod = RequestMethod.valueOf(method);
      String path = st.nextToken();
      input.requestPath = path;
    } catch (IOException e) {
      throw new RuntimeException();
    }
    super.run(outputStream, inputStream, input);
  }

}
