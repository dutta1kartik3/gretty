/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.ExecutorService
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 * Base task for starting jetty
 *
 * @author akhikhl
 */
abstract class StartBaseTask extends DefaultTask {

  boolean interactive = true
  boolean debug = false

  private JacocoHelper jacocoHelper
  
  protected final List<Closure> prepareServerConfigClosures = []
  protected final List<Closure> prepareWebAppConfigClosures = []

  @TaskAction
  void action() {
    LauncherConfig config = getLauncherConfig()
    Launcher launcher = anyWebAppUsesSpringBoot(config.getWebAppConfigs()) ? new SpringBootLauncher(project, config) : new DefaultLauncher(project, config)
    launcher.launch()
  }
  
  protected final boolean anyWebAppUsesSpringBoot(Iterable<WebAppConfig> wconfigs) {
    wconfigs.find { wconfig -> 
      wconfig.springBoot || (wconfig.projectPath && ProjectUtils.isSpringBootApp(project.project(wconfig.projectPath)))
    }
  }
  
  protected final void doPrepareServerConfig(ServerConfig sconfig) {
    for(Closure c in prepareServerConfigClosures) {
      c = c.rehydrate(sconfig, c.owner, c.thisObject)
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }
  }
  
  protected final void doPrepareWebAppConfig(WebAppConfig wconfig) {
    for(Closure c in prepareWebAppConfigClosures) {
      c = c.rehydrate(wconfig, c.owner, c.thisObject)
      c.resolveStrategy = Closure.DELEGATE_FIRST
      c()
    }
  }

  protected boolean getDefaultJacocoEnabled() {
    false
  }

  protected boolean getIntegrationTest() {
    false
  }

  JacocoTaskExtension getJacoco() {
    if(jacocoHelper == null && project.extensions.findByName('jacoco')) {
      jacocoHelper = new JacocoHelper(this)
      jacocoHelper.jacoco.enabled = getDefaultJacocoEnabled()
    }
    jacocoHelper?.jacoco
  }

  protected final LauncherConfig getLauncherConfig() {
  
    def self = this
    def startConfig = getStartConfig()

    new LauncherConfig() {
        
      boolean getDebug() {
        self.debug
      }

      boolean getIntegrationTest() {
        self.getIntegrationTest()
      }
  
      boolean getInteractive() {
        self.getInteractive()
      }

      def getJacocoConfig() {
        self.jacoco
      }

      boolean getManagedClassReload() {
        self.getManagedClassReload(startConfig.getServerConfig())
      }

      ServerConfig getServerConfig() {
        startConfig.getServerConfig()
      }
  
      String getStopTaskName() {
        self.getStopTaskName()
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        startConfig.getWebAppConfigs()
      }
    }
  }
  
  protected boolean getManagedClassReload(ServerConfig sconfig) {
    sconfig.managedClassReload
  }

  protected abstract StartConfig getStartConfig()

  protected abstract String getStopTaskName()

  final void jacoco(Closure configureClosure) {
    getJacoco()?.with configureClosure
  }
  
  final void prepareServerConfig(Closure closure) {
    prepareServerConfigClosures.add(closure)
  }
  
  final void prepareWebAppConfig(Closure closure) {
    prepareWebAppConfigClosures.add(closure)
  }
}