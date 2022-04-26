package com.invidi.simplewebserver.context;

import java.util.HashMap;

import com.invidi.simplewebserver.annotations.RestController;

public class SimpleWebServerContext implements WebServerContext {

  private HashMap<String, Object> controllerMap;

  private String rootDir;
  private String indexFileName = "index.html";
  private static SimpleWebServerContext serverContext;

  private SimpleWebServerContext() {
    this.controllerMap = new HashMap<String, Object>();
  }

  public static WebServerContext getWebServerContext() {
    if (serverContext == null) {
      serverContext = new SimpleWebServerContext();
    }
    return serverContext;
  }

  @Override
  public void setStaticPath(String path) {
    this.rootDir = path;
  }

  @Override
  public void addController(Object controller) {
    if (controller == null || !controller.getClass().isAnnotationPresent(RestController.class))
      return;
    RestController restController = (RestController) controller.getClass().getAnnotation(RestController.class);
    String apiPath = restController.value();
    controllerMap.put(apiPath, controller);
  }

  @Override
  public boolean isPathConnectedToController(String path) {
    String[] pathParts = path.split("/");
    StringBuilder sb = new StringBuilder();
    for (String subPath : pathParts) {
      if (subPath.isEmpty()) {
        continue;
      }
      sb.append("/" + subPath);
      if (controllerMap.containsKey(sb.toString()))
        return true;
    }
    return false;
  }

  @Override
  public Object getControllerForPath(String path) {
    String[] pathParts = path.split("/");
    StringBuilder sb = new StringBuilder();
    for (String subPath : pathParts) {
      if (subPath.isEmpty()) {
        continue;
      }
      sb.append("/" + subPath);
      if (controllerMap.containsKey(sb.toString()))
        return controllerMap.get(sb.toString());
    }
    return null;
  }

  @Override
  public void setIndexFile(String filename) {
    this.indexFileName = filename;
  }

  @Override
  public String getIndexFileName() {
    return indexFileName;
  }

  @Override
  public String getStaticPath() {
    return rootDir;
  }

}
