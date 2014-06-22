/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 * Gradle task for starting jetty
 *
 * @author akhikhl
 */
class AppStartTask extends StartBaseTask {

  @Delegate
  private ServerConfig serverConfig = new ServerConfig()

  @Delegate
  private WebAppConfig webAppConfig = new WebAppConfig()
  
  protected String getCompatibleServletContainer(String servletContainer) {
    servletContainer
  }

  protected boolean getEffectiveInplace() {
    if(webAppConfig.inplace != null)
      webAppConfig.inplace
    else if(project.gretty.webAppConfig.inplace != null)
      project.gretty.webAppConfig.inplace
    else
      true
  }
  
  @Override
  protected StartConfig getStartConfig() {

    ServerConfig sconfig = new ServerConfig()
    ConfigUtils.complementProperties(sconfig, serverConfig, project.gretty.serverConfig, ServerConfig.getDefault(project))
    sconfig.servletContainer = getCompatibleServletContainer(sconfig.servletContainer)
    sconfig.resolve(project)
    doPrepareServerConfig(sconfig)

    WebAppConfig wconfig = new WebAppConfig()
    ConfigUtils.complementProperties(wconfig, webAppConfig, project.gretty.webAppConfig, WebAppConfig.getDefaultForProject(project), new WebAppConfig(inplace: true))    
    wconfig.resolve(project, sconfig.servletContainer)
    doPrepareWebAppConfig(wconfig)

    new StartConfig() {

      @Override
      ServerConfig getServerConfig() {
        sconfig
      }

      @Override
      Iterable<WebAppConfig> getWebAppConfigs() {
        [ wconfig ]
      }
    }
  }

  @Override
  protected String getStopTaskName() {
    'appStop'
  }
}
