package org.eclipse.jetty.deploy.remote;

import java.nio.file.Paths;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractTest
{
    public static final String JETTY_VERSION = "9.4.27.v20200227";

    private HttpClient client;

    public String getJettyVersion()
    {
        String jettyVersion = System.getProperty("jetty.version");
        if (StringUtil.isBlank(jettyVersion))
        {
            jettyVersion = JETTY_VERSION;
        }
        return jettyVersion;
    }

    public String getDependencyVersion(String depName)
    {
        String version = System.getProperty(depName + ".version");
        if (StringUtil.isBlank(version))
        {
            throw new RuntimeException("surefire configuration missing " + depName + ".version system property");
        }
        return version;
    }

    public String getProjectVersion()
    {
        return getDependencyVersion("project");
    }

    public String getMavenLocalRepoPath()
    {
        String localRepoPath = System.getProperty("mavenRepoPath");
        if (StringUtil.isBlank(localRepoPath))
        {
            return Paths.get(System.getProperty("user.home"), ".m2/repository").toString();
        }
        return localRepoPath;
    }

    protected HttpClient startHttpClient() throws Exception
    {
        client = new HttpClient();
        client.start();
        return client;
    }

    @AfterEach
    public void stopClient()
    {
        LifeCycle.stop(client);
    }

    protected void assertHttpResponseOK(ContentResponse response)
    {
        if (response.getStatus() != HttpStatus.OK_200)
        {
            System.err.printf("Requested: %s%n", response.getRequest().getURI());
            System.err.printf("%s %s %s%n", response.getVersion(), response.getStatus(), response.getReason());
            System.err.println(response.getHeaders());
            System.err.println(response.getContentAsString());
            assertEquals(HttpStatus.OK_200, response.getStatus());
        }
    }
}
