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

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

public class RemoteAppProvider extends AbstractLifeCycle implements AppProvider
{
    private Path webAppDirectory;
    private DeploymentManager deploymentManager;

    public RemoteAppProvider()
    {
    }

    public void setWebAppDirectory(Path webappDirectory)
    {
        this.webAppDirectory = webappDirectory.toAbsolutePath();

        if (!Files.exists(webappDirectory))
        {
            throw new IllegalStateException("Required directory does not exist: " + this.webAppDirectory);
        }

        if (!Files.isDirectory(webappDirectory))
        {
            throw new IllegalStateException("Not a directory: " + this.webAppDirectory);
        }
    }

    public RemoteApp createApp(Path warPath, String contextPath)
    {
        return new RemoteApp(this.deploymentManager, this, warPath, contextPath);
    }

    public Path getWar(String warFilename)
    {
        if (StringUtil.isBlank(warFilename))
        {
            throw new IllegalStateException("War filename is blank");
        }

        Path path = webAppDirectory.resolve(warFilename);
        if (!path.startsWith(webAppDirectory))
        {
            throw new IllegalStateException("Unable to deploy " + path + ", it is not within webappDirectory " + webAppDirectory);
        }
        return path;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager)
    {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception
    {
        WebAppContext webAppContext = new WebAppContext();

        if (app instanceof RemoteApp)
        {
            RemoteApp remoteApp = (RemoteApp)app;
            webAppContext.setContextPath(remoteApp.getContextPath());
            webAppContext.setWarResource(remoteApp.getWarResource());
        }
        else
        {
            throw new IllegalStateException(app.getClass().getName() + " is not an instance of " + RemoteApp.class.getName());
        }

        return webAppContext;
    }
}
