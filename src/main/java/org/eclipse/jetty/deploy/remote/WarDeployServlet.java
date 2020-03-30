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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class WarDeployServlet extends AbstractDeploymentServlet
{
    public static final Logger LOG = Log.getLogger(WarDeployServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();

        String contextPath = request.getParameter("path");
        String warFilename = request.getParameter("war");

        List<App> apps = findAppsByContextPath(contextPath);
        if (!apps.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            writer.println("An application is already deployed at this context : " + contextPath);
            return;
        }

        Path path = appProvider.getWar(warFilename);
        if (!Files.exists(path) || !Files.isRegularFile(path))
        {
            LOG.warn("Unable to find {}", path);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.println("Unable to find war: " + warFilename);
            return;
        }

        RemoteApp remoteApp = appProvider.createApp(path, contextPath);
        deploymentManager.addApp(remoteApp);
        deploymentManager.requestAppGoal(remoteApp, AppLifeCycle.STARTED);

        writer.println("Webapp deployed at context " + contextPath);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();

        String contextPath = request.getParameter("path");

        List<App> apps = findAppsByContextPath(contextPath);
        if (!apps.isEmpty())
        {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            writer.println("An application is already deployed at this context : " + contextPath);
            return;
        }

        String filename = "UNKNOWN";
        if (contextPath.equals("/"))
        {
            filename = "ROOT";
        }
        else if (contextPath.length() > 1)
        {
            filename = contextPath.substring(1);
        }

        Path path = appProvider.getWar(filename + ".war");

        try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
             InputStream input = request.getInputStream())
        {
            IO.copy(input, output);
        }

        RemoteApp remoteApp = appProvider.createApp(path, contextPath);
        deploymentManager.addApp(remoteApp);
        deploymentManager.requestAppGoal(remoteApp, AppLifeCycle.STARTED);

        writer.println("Webapp deployed at context " + contextPath);
    }
}
