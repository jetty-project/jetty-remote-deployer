# DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables Remote WebApp deployment via remote-control webapp.

[depend]
deploy
webapp

[lib]
lib/jetty-remote-deploymentmanager-*.jar

[files]
remote-webapps/

[xml]
etc/jetty-deploy-remote.xml

[ini-template]
# Monitored directory path (fully qualified)
# jetty.deploy.remote.path=/var/www/remote-webapps
