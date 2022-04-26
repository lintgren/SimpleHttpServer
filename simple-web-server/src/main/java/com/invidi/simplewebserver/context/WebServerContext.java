package com.invidi.simplewebserver.context;

public interface WebServerContext {

   public void setStaticPath(String path);

   public void setIndexFile(String filename);

   public String getIndexFileName();

   public String getStaticPath();

   public boolean isPathConnectedToController(String path);

   public void addController(Object controller);

   public Object getControllerForPath(String path);
}
