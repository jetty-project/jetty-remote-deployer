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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class InitializationServlet extends HttpServlet
{
    public static final Logger LOG = Log.getLogger(InitializationServlet.class);

    @Override
    public void init() throws ServletException
    {
        ServletContext servletContext = getServletContext();

        DeploymentManager deploymentManager = (DeploymentManager)servletContext.getAttribute("org.eclipse.jetty.deploy.DeploymentManager");

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

        try
        {
            // Stop DeploymentManager, we need to modify it.
            LifeCycle.stop(deploymentManager);
            RemoteAppProvider remoteAppProvider = new RemoteAppProvider(configHome.resolve("remote-webapps"));
            deploymentManager.addAppProvider(remoteAppProvider);

            servletContext.setAttribute(RemoteAppProvider.class.getName(), remoteAppProvider);

            LOG.debug("Initialized the Remote Jetty deployer via {}", remoteAppProvider);
        }
        finally
        {
            // Start DeploymentManager
            LifeCycle.start(deploymentManager);
        }
    }

    private Path getPathProperty(String key)
    {
        String value = System.getProperty(key);
        if (value == null)
            return null;
        return Paths.get(value);
    }
}
