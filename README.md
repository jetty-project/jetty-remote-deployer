# Jetty Remote Deployer WebApp

This project provides a WAR file that provides HTTP resource endpoints to deploy and undeploy
other WebApps.

HTTP Resource Endpoints:

## Resource /deploy

Method `GET` will deploy the referenced war file (found on the server) to the running instance of Jetty.

 * Parameter `path` is required and is the destination context-path of the deployed webapp
 * Parameter `war` is required and is the filename reference to the WAR file on the server that should be deployed.

Method `PUT` will perform a remote deploy of the provided war to the running instance of Jetty.

 * Parameter `path` is required and is the destination context-path of the deployed webapp
 * Request body content is the binary of the WAR file.
 * Request `Content-Type` field should be `application/octet-stream`

## Resource /undeploy

Method `GET` will undeploy the provided context-path

 * Parameter `path` is required and is the destination context-path of the deployed webapp.
 
## Configuration

This webapp expects that the Jetty `DeploymentManager` is properly installed and available.

If you are using modern jetty-home archive (or the older jetty-distribution archive) you can
check the results of `--list-config` for some key entries.

```
$ cd /path/to/myjettybase
$ java -jar /path/to/jetty-home-9.4.27.v20200227/start.jar --list-config

...(snip)...

Jetty Server Classpath:
-----------------------
Version Information on 11 entries in the classpath.
Note: order presented here is how they would appear on the classpath.
      changes to the --module=name command line options will be reflected here.
 0:                    3.1.0 | ${jetty.home}/lib/servlet-api-3.1.jar
 1:                 3.1.0.M0 | ${jetty.home}/lib/jetty-schemas-3.1.jar
 2:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-http-9.4.27.v20200227.jar
 3:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-server-9.4.27.v20200227.jar
 4:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-xml-9.4.27.v20200227.jar
 5:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-util-9.4.27.v20200227.jar
 6:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-io-9.4.27.v20200227.jar
 7:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-security-9.4.27.v20200227.jar
 8:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-servlet-9.4.27.v20200227.jar
 9:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-webapp-9.4.27.v20200227.jar
10:         9.4.27.v20200227 | ${jetty.home}/lib/jetty-deploy-9.4.27.v20200227.jar

Jetty Active XMLs:
------------------
 ${jetty.home}/etc/jetty-bytebufferpool.xml
 ${jetty.home}/etc/jetty-threadpool.xml
 ${jetty.home}/etc/jetty.xml
 ${jetty.home}/etc/jetty-webapp.xml
 ${jetty.home}/etc/jetty-deploy.xml
 ${jetty.home}/etc/jetty-http.xml
```

The existence of `jetty-deploy-9.4.27.v20200227.jar` and `jetty-deploy.xml` is what you are looking for.