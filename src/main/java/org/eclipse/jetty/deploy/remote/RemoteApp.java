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

import java.nio.file.Path;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;

public class RemoteApp extends App
{
    private Path warPath;
    private String contextPath;

    public RemoteApp(DeploymentManager manager, AppProvider provider, Path warFile, String contextPath)
    {
        super(manager, provider, contextPath);
        this.warPath = warFile;
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath()
    {
        return contextPath;
    }

    public Path getWarPath()
    {
        return warPath;
    }

    public Resource getWarResource()
    {
        return new PathResource(warPath);
    }
}
