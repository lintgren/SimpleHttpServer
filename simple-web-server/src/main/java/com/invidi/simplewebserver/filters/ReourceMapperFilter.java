package com.invidi.simplewebserver.filters;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invidi.simplewebserver.Exceptions.SimpleWebServerException;
import com.invidi.simplewebserver.annotations.Path;
import com.invidi.simplewebserver.annotations.QueryParam;
import com.invidi.simplewebserver.context.WebServerContext;
import com.invidi.simplewebserver.model.HttpStatus;
import com.invidi.simplewebserver.model.RequestMethod;

public class ReourceMapperFilter extends RequestFilterDecorator {
  private WebServerContext webServerContext;

  public ReourceMapperFilter(RequestFilter requestFilter, WebServerContext context) {
    super(requestFilter);
    this.webServerContext = context;
  }

  @Override
  public void run(OutputStream outputStream, InputStream inputStream, HttpObject input) {
    if (webServerContext.isPathConnectedToController(input.requestPath)) {
      // API POST/GET
      ObjectMapper om = new ObjectMapper();
      Object controller = webServerContext.getControllerForPath(input.requestPath);
      Method[] controllerMethods = controller.getClass().getMethods();
      for (Method controllerMethod : controllerMethods) {
        Path methodPathAnnotation = controllerMethod.getAnnotation(Path.class);
        if (methodPathAnnotation == null)
          continue;
        RequestMethod methodRequestMethod = methodPathAnnotation.method();
        String methodPathValue = methodPathAnnotation.value();
        if (methodRequestMethod.equals(input.requestMethod) &&
            input.requestPath.contains(methodPathValue)) {
          int queryStart = input.requestPath.indexOf('?', 0);
          String query = input.requestPath.substring(queryStart + 1);
          String[] queryArray = query.split("=|&");
          Map<String, String> queryKeyValue = new HashMap<>();
          if (queryArray.length % 2 != 0) {
            // check so we get values with queryparams
            throw new SimpleWebServerException(HttpStatus.BAD_REQUEST, "Missing value");
          }
          for (int i = 0; i < queryArray.length; i += 2) {
            String decodedKey;
            try {
              decodedKey = URLDecoder.decode(queryArray[i], StandardCharsets.UTF_8.toString());
              String decodedValue = URLDecoder.decode(queryArray[i + 1], StandardCharsets.UTF_8.toString());
              queryKeyValue.put(decodedKey, decodedValue);
            } catch (UnsupportedEncodingException e) {
              // TODO Auto-generated catch block
              throw new SimpleWebServerException(HttpStatus.BAD_REQUEST, "Could not decode URLQuery");
            }

          }
          Parameter[] methodParameters = controllerMethod.getParameters();
          Object[] sortedQueryValues = new String[methodParameters.length];
          IntStream.range(0, methodParameters.length)
              .forEach(idx -> sortedQueryValues[idx] = queryKeyValue
                  .get(methodParameters[idx].getAnnotation(QueryParam.class).value()));
          try {
            JsonNode result = om.convertValue(
                controllerMethod.invoke(controller, sortedQueryValues), JsonNode.class);
            input.responseHeader = getJsonHeader(result);
            input.responseBody = result.toString();
          } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new SimpleWebServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error converting result");
          }
        }
      }
    } else if (input.requestMethod == null || input.requestMethod == RequestMethod.GET) {
      // Static Files
      if (input.requestPath == null || input.requestPath.equals("/")) {
        File requestedFile;
        URL resource = getClass()
            .getResource(webServerContext.getStaticPath() + "/" + webServerContext.getIndexFileName());
        if (resource == null) {
          throw new SimpleWebServerException(HttpStatus.NOT_FOUND, "No souch file or not permitted");
        } else {
          requestedFile = new File(resource.getFile());
        }
        input.responseHeader = getFileHeader(requestedFile);
        input.responseFile = getFileData(requestedFile);
      } else {
        // Fallback to Index.html
        File requestedFile = new File(webServerContext.getStaticPath(), input.requestPath);
        URL resource = getClass()
            .getResource(webServerContext.getStaticPath() + "/" + input.requestPath);
        if (resource == null) {
          throw new SimpleWebServerException(HttpStatus.NOT_FOUND, "No souch file or not permitted");
        } else {
          requestedFile = new File(resource.getFile());
        }
        input.responseHeader = getFileHeader(requestedFile);
        input.responseFile = getFileData(requestedFile);
      }
    }
    super.run(outputStream, inputStream, input);

  }

  private String getJsonHeader(JsonNode requestedJson) {
    ObjectMapper om = new ObjectMapper();
    try {
      byte[] byteResult = om.writeValueAsBytes(requestedJson);
      StringBuilder out = new StringBuilder();
      out.append("HTTP/1.0 200 OK\r\n");
      Date now = new Date();
      out.append("Date: " + now + "\r\n");
      out.append("Server: SimpleWeb/1.0" + "\r\n");
      out.append("Accept: " + "application/json");
      out.append("Content-length: " + byteResult.length + "\r\n");
      out.append("Content-type: " + "application/json" + "\r\n\r\n");

      return out.toString();
    } catch (JsonProcessingException e) {
      throw new SimpleWebServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create header");
    }
  }

  private byte[] getFileData(File requestedFile) {
    try (
        DataInputStream fis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(requestedFile)))) {
      if (!requestedFile.canRead()) {
        throw new SimpleWebServerException(HttpStatus.UNAUTHORIZED, "Could not read file");
      }
      byte[] theData = new byte[(int) requestedFile.length()];
      fis.readFully(theData);
      fis.close();
      return theData;
    } catch (FileNotFoundException e) {
      throw new SimpleWebServerException(HttpStatus.NOT_FOUND, "Could not find requested file");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getFileHeader(File requestedFile) {
    try (
        DataInputStream fis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(requestedFile)))) {
      if (!requestedFile.canRead()) {
        throw new SimpleWebServerException(HttpStatus.UNAUTHORIZED, "Could not read file");
      }
      byte[] theData = new byte[(int) requestedFile.length()];
      fis.readFully(theData);
      fis.close();
      StringBuilder out = new StringBuilder();
      out.append("HTTP/1.0 200 OK\r\n");
      Date now = new Date();
      out.append("Date: " + now + "\r\n");
      out.append("Server: SimpleWeb/1.0" + "\r\n");
      out.append("Content-length: " + theData.length + "\r\n");
      out.append("Content-type: " + "text/html" + "\r\n\r\n");
      return out.toString();
    } catch (FileNotFoundException e) {
      throw new SimpleWebServerException(HttpStatus.NOT_FOUND, "Could not find requested file");
    } catch (Exception e) {
      throw new SimpleWebServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not creating header");
    }
  }
}
