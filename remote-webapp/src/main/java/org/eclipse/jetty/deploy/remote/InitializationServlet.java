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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class InitializationServlet extends HttpServlet
{
    public static final Logger LOG = Log.getLogger(InitializationServlet.class);

    @Override
    public void init()
    {
        ServletContext servletContext = getServletContext();

        Server server = (Server)servletContext.getAttribute("org.eclipse.jetty.server.Server");

        DeploymentManager deploymentManager = server.getBean(DeploymentManager.class);
        if (deploymentManager == null)
        {
            throw new IllegalStateException("Unable to find Jetty DeploymentManager (did you forget to enable the Jetty deploy module?)");
        }

        RemoteAppProvider remoteAppProvider = deploymentManager.getBean(RemoteAppProvider.class);
        if (remoteAppProvider == null)
        {
            throw new IllegalStateException("RemoteAppProvider not installed (did you forget to enable deploy-remote jetty module)");
        }

        servletContext.setAttribute(RemoteAppProvider.class.getName(), remoteAppProvider);
        servletContext.setAttribute(DeploymentManager.class.getName(), deploymentManager);

        LOG.debug("Initialized the Remote Jetty deployer via {}", remoteAppProvider);
    }
}
