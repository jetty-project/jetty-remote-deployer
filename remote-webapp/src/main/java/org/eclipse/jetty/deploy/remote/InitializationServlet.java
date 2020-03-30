//
//  ========================================================================
//  Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.deploy.remote;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class InitializationServlet extends HttpServlet
{
    public static final Logger LOG = Log.getLogger(InitializationServlet.class);

    @Override
    public void init() throws ServletException
    {
        ServletContext servletContext = getServletContext();

        Server server = (Server)servletContext.getAttribute("org.eclipse.jetty.server.Server");

        DeploymentManager deploymentManager = server.getBean(DeploymentManager.class);

        if (deploymentManager == null)
        {
            throw new IllegalStateException("Unable to find Jetty DeploymentManager (did you forget to enable the Jetty deploy module?)");
        }

        Path configHome = getPathProperty("config.home");

        if (configHome == null)
        {
            // Check Jetty start.jar property "jetty.base"
            configHome = getPathProperty("jetty.base");
        }

        if (configHome == null)
        {
            throw new IllegalStateException("Cannot find the Jetty configuration home");
        }

        // NOTE: we NEVER use `jetty.home` as it is supposed to be treated as a read-only directory.
        Path jettyHome = getPathProperty("jetty.home");
        if (jettyHome != null)
        {
            try
            {
                if (Files.isSameFile(jettyHome, configHome))
                {
                    throw new IllegalStateException("Modifying jetty.home " + jettyHome + " is not supported.");
                }
            }
            catch (IOException ignore)
            {
                // ignore
            }
        }

        // Stop DeploymentManager, we need to modify it.
        RemoteAppProvider remoteAppProvider = new RemoteAppProvider();
        remoteAppProvider.setWebAppDirectory(configHome.resolve("remote-webapps"));
        try
        {
            // Try simple call first
            deploymentManager.addAppProvider(remoteAppProvider);
        }
        catch (IllegalStateException e)
        {
            // The DeploymentManager is running, cannot modify it at runtime
            try
            {
                Field providersField = DeploymentManager.class.getDeclaredField("_providers");
                providersField.setAccessible(true);
                //noinspection unchecked
                List<AppProvider> providers = (List<AppProvider>)providersField.get(deploymentManager);
                providers.add(remoteAppProvider);
                providersField.set(deploymentManager, providers);
                deploymentManager.addBean(remoteAppProvider);
            }
            catch (NoSuchFieldException | IllegalAccessException e2)
            {
                e2.printStackTrace();
            }
        }

        servletContext.setAttribute(RemoteAppProvider.class.getName(), remoteAppProvider);
        servletContext.setAttribute(DeploymentManager.class.getName(), deploymentManager);

        LOG.debug("Initialized the Remote Jetty deployer via {}", remoteAppProvider);
    }

    private Path getPathProperty(String key)
    {
        String value = System.getProperty(key);
        if (value == null)
            return null;
        return Paths.get(value);
    }
}
