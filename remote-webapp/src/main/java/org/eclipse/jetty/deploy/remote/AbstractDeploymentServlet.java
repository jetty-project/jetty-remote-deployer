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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.util.StringUtil;

public abstract class AbstractDeploymentServlet extends HttpServlet
{
    /**
     * The Jetty DeploymentManager
     */
    protected DeploymentManager deploymentManager;

    /**
     * The RemoteAppProvider
     */
    protected RemoteAppProvider appProvider;

    @Override
    public void init() throws ServletException
    {
        super.init();

        appProvider = (RemoteAppProvider)getServletContext().getAttribute(RemoteAppProvider.class.getName());
        deploymentManager = (DeploymentManager)getServletContext().getAttribute(DeploymentManager.class.getName());

        if (appProvider == null || deploymentManager == null)
        {
            throw new ServletException(getServletContext().getServletContextName() + " not initialized properly");
        }
    }

    protected List<App> findAppsByContextPath(String contextPath)
    {
        if (StringUtil.isBlank(contextPath))
        {
            return Collections.emptyList();
        }

        List<App> apps = new ArrayList<>();

        for (DeploymentManager.AppEntry entry : deploymentManager.getAppEntries())
        {
            // Not all apps have a contextPath.
            if (contextPath.equals(entry.getApp().getContextPath()))
            {
                apps.add(entry.getApp());
            }
        }

        return apps;
    }
}
