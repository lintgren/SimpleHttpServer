package com.invidi.simplewebserver.filters;

import java.io.InputStream;
import java.io.OutputStream;

public interface RequestFilter {

  public void run(OutputStream stream, InputStream inputStream, HttpObject o);

}
