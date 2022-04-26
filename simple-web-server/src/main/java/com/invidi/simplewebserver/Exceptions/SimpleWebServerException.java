package com.invidi.simplewebserver.Exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.invidi.simplewebserver.model.HttpStatus;

public class SimpleWebServerException extends RuntimeException {
  @JsonProperty
  private HttpStatus statusCode;
  @JsonProperty
  private String message;

  public SimpleWebServerException(HttpStatus statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  public HttpStatus getStatusCode() {
    return statusCode;
  }

  public String getMessage() {
    return message;
  }

}
