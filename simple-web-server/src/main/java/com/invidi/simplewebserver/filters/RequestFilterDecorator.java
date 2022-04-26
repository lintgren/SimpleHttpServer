package com.invidi.simplewebserver.filters;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class RequestFilterDecorator implements RequestFilter {
  protected RequestFilter nextRequestFilter;

  public RequestFilterDecorator(RequestFilter requestFilter) {
    this.nextRequestFilter = requestFilter;
  }

  @Override
  public void run(OutputStream outputStream, InputStream inputStream, HttpObject o) {
    nextRequestFilter.run(outputStream, inputStream, o);
  }
}
