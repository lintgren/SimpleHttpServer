package com.invidi.simplewebserver.filters;

import com.invidi.simplewebserver.model.RequestMethod;

public class HttpObject {
  public RequestMethod requestMethod;
  public String requestPath;
  public String responseHeader;
  public String responseBody;
  public byte[] responseFile;

}
