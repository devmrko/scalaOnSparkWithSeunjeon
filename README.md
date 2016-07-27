# Scala example on spark with seunjeon library

### how to use scala, and sbt on eclipse IDE
 - preparation
   * install scala, sbt
     - scala version: 2.11.8
     - sbt version: 0.13
   * set system variables(for windows user)
     - SCALA_HOME, SBT_HOME
     - add binary folder of above libraries to path variable
   * install scala plugin on eclipse IDE
   * config sbt plugin
     - plugin name: sbteclipse
     - config file path: ~/.sbt/0.13/plugins/plugins.sbt
     - reference url: https://github.com/typesafehub/sbteclipse
     <pre><code>addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")</code></pre>
 
 - project configuration
   * create empty project
   * create build.sbt file at root project foler, and add dependencies you need
   * run below command in order to make an ecilpse project configuration
     <pre><code>sbt eclipse</code></pre>
   * refresh project

 - trouble shooting
   * in case eclipse needs to use hadoop library, you could add hadoop-common-2.2.0-bin-master as windows' system variables.
      - reference URL: www.srccodes.com/p/article/39/error-util-shell-failed-locate-winutils-binary-hadoop-binary-path
    
