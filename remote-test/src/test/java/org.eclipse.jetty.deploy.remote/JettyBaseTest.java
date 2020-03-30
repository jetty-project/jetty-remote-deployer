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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.PathContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.tests.JettyHomeTester;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JettyBaseTest extends AbstractTest
{
    @Test
    public void testDeployLoga() throws Exception
    {
        JettyHomeTester jetty = JettyHomeTester.Builder.newInstance()
            .jettyVersion(getJettyVersion())
            .mavenLocalRepository(getMavenLocalRepoPath())
            .build();

        // Unpack remote dist archive
        File distArchive = jetty.resolveArtifact("org.eclipse.jetty:jetty-remote-webapp:jar:config:" + getProjectVersion());
        jetty.installConfigurationJar(distArchive);

        String[] setupArgs = {
            "--approve-all-licenses",
            "--add-to-start=http,deploy,deploy-remote"
        };

        // Setup the jetty instance
        try (JettyHomeTester.Run setup = jetty.start(setupArgs))
        {
            assertTrue(setup.awaitFor(5, TimeUnit.SECONDS));
            assertEquals(0, setup.getExitValue());

            int httpPort = jetty.freePort();

            String[] runArgs = {
                "jetty.http.port=" + httpPort
            };

            // Run the server instance
            try (JettyHomeTester.Run run = jetty.start(runArgs))
            {
                assertTrue(run.awaitConsoleLogsFor("Started @", 3, TimeUnit.SECONDS));

                URI serverURI = URI.create("http://localhost:" + httpPort + "/");

                HttpClient client = startHttpClient();

                Path warPath = MavenTestingUtils.getTestResourcePathFile("wars/log-a.war");

                // Perform a remote deploy
                URI deployUri = serverURI.resolve("/jetty-remote-deployer/deploy?path=/loga");
                ContentResponse response = client.newRequest(deployUri)
                    .method(HttpMethod.PUT)
                    .content(new PathContentProvider(warPath))
                    .send();

                assertThat("Response status", response.getStatus(), is(200));

                // Test that it is there
                response = client.GET(serverURI.resolve("/loga/logging"));
                assertThat("Response status", response.getStatus(), is(200));

                // Undeploy
                URI undeployUri = serverURI.resolve("/jetty-remote-deployer/undeploy?path=/loga");
                response = client.GET(undeployUri);
                assertThat("Response status", response.getStatus(), is(200));

                // Test that it is NOT there
                response = client.GET(serverURI.resolve("/loga/logging"));
                assertThat("Response status", response.getStatus(), is(404));
            }
        }
    }
}
