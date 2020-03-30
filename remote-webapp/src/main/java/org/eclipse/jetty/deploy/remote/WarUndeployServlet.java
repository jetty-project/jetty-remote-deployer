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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;

public class WarUndeployServlet extends AbstractDeploymentServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();

        String contextPath = request.getParameter("path");

        List<App> apps = findAppsByContextPath(contextPath);
        if (apps.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.println("An application is not deployed at context : " + contextPath);
            return;
        }

        for (App app : apps)
        {
            deploymentManager.requestAppGoal(app, AppLifeCycle.UNDEPLOYED);
            deploymentManager.removeApp(app);
            if (app instanceof RemoteApp)
            {
                Files.deleteIfExists(((RemoteApp)app).getWarPath());
            }
        }
    }
}
