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
 
## Installation

There is a provided `jetty.base` config overlay that you will need if using the modern jetty-home
standalone (or the older jetty-distribution standalone).

```
$ ls remote-webapp/target/*.jar
remote-webapp/target/jetty-remote-webapp-0.1-SNAPSHOT-config.jar
```

The process goes like this ...

```
$ cd /path/to/myjettybase
$ jar -xf /path/to/jetty-remote-webapp-0.1-SNAPSHOT-config.jar 
$ java -jar /path/to/jetty-home-9.4.27.v20200227/start.jar --add-to-start=deploy-remote
INFO  : deploy-remote   initialized in ${jetty.base}/start.d/deploy-remote.ini
INFO  : webapp          transitively enabled, ini template available with --add-to-start=webapp
INFO  : security        transitively enabled
INFO  : servlet         transitively enabled
INFO  : deploy          transitively enabled, ini template available with --add-to-start=deploy
MKDIR : ${jetty.base}/remote-webapps
INFO  : Base directory was modified
```

Check the results with `--list-config`

```
$ cd /path/to/myjettybase
$ java -jar /path/to/jetty-home-9.4.27.v20200227/start.jar --list-config

...(snip)...

Jetty Server Classpath:
-----------------------
Version Information on 12 entries in the classpath.
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
11:             0.1-SNAPSHOT | ${jetty.base}/lib/jetty-remote-deploymentmanager-0.1-SNAPSHOT.jar

Jetty Active XMLs:
------------------
 ${jetty.home}/etc/jetty-bytebufferpool.xml
 ${jetty.home}/etc/jetty-threadpool.xml
 ${jetty.home}/etc/jetty.xml
 ${jetty.home}/etc/jetty-webapp.xml
 ${jetty.home}/etc/jetty-deploy.xml
 ${jetty.base}/etc/jetty-deploy-remote.xml
 ${jetty.home}/etc/jetty-http.xml
```

The existence of the following entries are important:
 
  * `jetty-deploy-9.4.27.v20200227.jar`
  * `jetty-deploy.xml`
  * `jetty-remote-deploymentmanager-0.1-SNAPSHOT.jar`
  
Other important files / directories:

  * `${jetty.base}/remote-webapps/` - this is the "work directory" for the RemoteAppProvider, and
    is where all of the webapps that are under its control reside.
    We intentionally do not use `${jetty.base}/webapps/` as that is controlled by a different
    app provider and it's not sane to co-mingle them.
  * `${jetty.base}/webapps/remote-control.war` - this is the webapp with the mentioned http
    resource endpoints from the start of this document
  * `${jetty.base}/webapps/remote-control.xml` - this is the configuration for the 
    remote-control webapp, you can edit this for security, virtual hosts, context-path, etc 