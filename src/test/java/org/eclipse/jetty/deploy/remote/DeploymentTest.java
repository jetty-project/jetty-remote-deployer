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

import java.net.URI;
import java.nio.file.Path;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.jupiter.WorkDir;
import org.eclipse.jetty.toolchain.test.jupiter.WorkDirExtension;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(WorkDirExtension.class)
public class DeploymentTest
{
    private Server server;
    private HttpClient client;
    public WorkDir workDir;

    @BeforeEach
    public void startServer() throws Exception
    {
        Path jettyBase = workDir.getEmptyPathDir().resolve("jettyBase");
        FS.ensureEmpty(jettyBase);
        System.setProperty("jetty.base", jettyBase.toString());

        Path remoteWebApps = jettyBase.resolve("remote-webapps");
        FS.ensureEmpty(remoteWebApps);

        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        // Base level Handlers
        HandlerList handlers = new HandlerList();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.addHandler(contexts);
        handlers.addHandler(new DefaultHandler());
        server.setHandler(handlers);

        // Enable DeploymentManager, but without the default AppProvider
        DeploymentManager deploymentManager = new DeploymentManager();
        deploymentManager.setContexts(contexts);
        deploymentManager.setContextAttribute(
            "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
            ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
        server.addBean(deploymentManager);

        // Add Remote Deployer
        WebAppContext remoteDeployerContext = new WebAppContext();
        remoteDeployerContext.setContextPath("/jetty-remote-deployer");
        Path srcMainWebApp = MavenTestingUtils.getProjectDirPath("src/main/webapp");
        remoteDeployerContext.setBaseResource(new PathResource(srcMainWebApp));
        Path targetClasses = MavenTestingUtils.getTargetPath("classes");
        remoteDeployerContext.setExtraClasspath(targetClasses.toString());
        contexts.addHandler(remoteDeployerContext);

        server.start();
    }

    @BeforeEach
    public void startClient() throws Exception
    {
        client = new HttpClient();
        client.start();
    }

    @AfterEach
    public void stopServerAndClient()
    {
        LifeCycle.stop(server);
        LifeCycle.stop(client);
    }

    @Test
    public void testDeployNotUploadedYet() throws Exception
    {
        URI deployUri = server.getURI().resolve("/jetty-remote-deployer/deploy?path=/loga&war=loga");

        ContentResponse response = client.GET(deployUri);
        assertThat("Response status", response.getStatus(), is(404));
    }

    @Test
    public void testRemoteDeployLogA() throws Exception
    {
        Path warPath = MavenTestingUtils.getTestResourcePathFile("wars/log-a.war");

        // Perform a remote deploy
        URI deployUri = server.getURI().resolve("/jetty-remote-deployer/deploy?path=/loga");
        ContentResponse response = client.newRequest(deployUri)
            .method(HttpMethod.PUT)
            .content(new PathContentProvider(warPath))
            .send();

        assertThat("Response status", response.getStatus(), is(200));

        // Test that it is there
        response = client.GET(server.getURI().resolve("/loga/logging"));
        System.out.println(response.getContentAsString());
        assertThat("Response status", response.getStatus(), is(200));
    }

    @Test
    public void testRemoteDeployLogAUndeployLogA() throws Exception
    {
        Path warPath = MavenTestingUtils.getTestResourcePathFile("wars/log-a.war");

        // Perform a remote deploy
        URI deployUri = server.getURI().resolve("/jetty-remote-deployer/deploy?path=/loga");
        ContentResponse response = client.newRequest(deployUri)
            .method(HttpMethod.PUT)
            .content(new PathContentProvider(warPath))
            .send();

        assertThat("Response status", response.getStatus(), is(200));

        // Test that it is there
        response = client.GET(server.getURI().resolve("/loga/logging"));
        assertThat("Response status", response.getStatus(), is(200));

        // Undeploy
        URI undeployUri = server.getURI().resolve("/jetty-remote-deployer/undeploy?path=/loga");
        response = client.GET(undeployUri);
        assertThat("Response status", response.getStatus(), is(200));

        // Test that it is NOT there
        response = client.GET(server.getURI().resolve("/loga/logging"));
        assertThat("Response status", response.getStatus(), is(404));
    }
}
