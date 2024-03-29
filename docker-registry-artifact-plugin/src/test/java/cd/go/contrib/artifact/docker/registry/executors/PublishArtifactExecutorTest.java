/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.artifact.docker.registry.executors;

import cd.go.contrib.artifact.docker.registry.ConsoleLogger;
import cd.go.contrib.artifact.docker.registry.DockerClientFactory;
import cd.go.contrib.artifact.docker.registry.DockerProgressHandler;
import cd.go.contrib.artifact.docker.registry.model.ArtifactPlan;
import cd.go.contrib.artifact.docker.registry.model.ArtifactStore;
import cd.go.contrib.artifact.docker.registry.model.ArtifactStoreConfig;
import cd.go.contrib.artifact.docker.registry.model.PublishArtifactRequest;
import cd.go.plugin.base.test_helper.system_extensions.annotations.EnvironmentVariable;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PublishArtifactExecutorTest {
    @Mock
    private GoPluginApiRequest request;
    @Mock
    private ConsoleLogger consoleLogger;
    @Mock
    private DockerProgressHandler dockerProgressHandler;
    @Mock
    private DefaultDockerClient dockerClient;
    @Mock
    private DockerClientFactory dockerClientFactory;

    private File agentWorkingDir;

    @BeforeEach
    void setUp(@TempDir File tempDir) throws InterruptedException, DockerException, DockerCertificateException {
        initMocks(this);
        agentWorkingDir = tempDir;

        when(dockerClientFactory.docker(any())).thenReturn(dockerClient);
    }

    @Test
    void shouldPublishArtifactUsingBuildFile() throws IOException, DockerException, InterruptedException {
        final ArtifactPlan artifactPlan = new ArtifactPlan("id", "storeId", "build.json");
        final ArtifactStoreConfig storeConfig = new ArtifactStoreConfig("localhost:5000", "other", "admin", "admin123");
        final ArtifactStore artifactStore = new ArtifactStore(artifactPlan.getId(), storeConfig);
        final PublishArtifactRequest publishArtifactRequest = new PublishArtifactRequest(artifactStore, artifactPlan, agentWorkingDir.getAbsolutePath());

        Path path = Paths.get(agentWorkingDir.getAbsolutePath(), "build.json");
        Files.write(path, "{\"image\":\"localhost:5000/alpine\",\"tag\":\"3.6\"}".getBytes());

        when(request.requestBody()).thenReturn(publishArtifactRequest.toJSON());
        when(dockerProgressHandler.getDigest()).thenReturn("foo");

        final GoPluginApiResponse response = new PublishArtifactExecutor(request, consoleLogger, dockerProgressHandler, dockerClientFactory).execute();

        verify(dockerClient).push(eq("localhost:5000/alpine:3.6"), any(ProgressHandler.class));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"metadata\":{\"image\":\"localhost:5000/alpine:3.6\",\"digest\":\"foo\"}}");
    }

    @Test
    void shouldPublishArtifactUsingImageAndTag() throws IOException, DockerException, InterruptedException {
        final ArtifactPlan artifactPlan = new ArtifactPlan("id", "storeId", "alpine", Optional.of("3.6"));
        final ArtifactStoreConfig storeConfig = new ArtifactStoreConfig("localhost:5000", "other", "admin", "admin123");
        final ArtifactStore artifactStore = new ArtifactStore(artifactPlan.getId(), storeConfig);
        final PublishArtifactRequest publishArtifactRequest = new PublishArtifactRequest(artifactStore, artifactPlan, agentWorkingDir.getAbsolutePath());

        when(request.requestBody()).thenReturn(publishArtifactRequest.toJSON());
        when(dockerProgressHandler.getDigest()).thenReturn("foo");

        final GoPluginApiResponse response = new PublishArtifactExecutor(request, consoleLogger, dockerProgressHandler, dockerClientFactory).execute();

        verify(dockerClient).push(eq("alpine:3.6"), any(ProgressHandler.class));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"metadata\":{\"image\":\"alpine:3.6\",\"digest\":\"foo\"}}");
    }

    @Test
    void shouldAddErrorToPublishArtifactResponseWhenFailedToPublishImage() throws IOException, DockerException, InterruptedException {
        final ArtifactPlan artifactPlan = new ArtifactPlan("id", "storeId", "build.json");
        final ArtifactStoreConfig artifactStoreConfig = new ArtifactStoreConfig("localhost:5000", "other", "admin", "admin123");
        final ArtifactStore artifactStore = new ArtifactStore(artifactPlan.getId(), artifactStoreConfig);
        final PublishArtifactRequest publishArtifactRequest = new PublishArtifactRequest(artifactStore, artifactPlan, agentWorkingDir.getAbsolutePath());
        final ArgumentCaptor<DockerProgressHandler> argumentCaptor = ArgumentCaptor.forClass(DockerProgressHandler.class);

        Path path = Paths.get(agentWorkingDir.getAbsolutePath(), "build.json");
        Files.write(path, "{\"image\":\"localhost:5000/alpine\",\"tag\":\"3.6\"}".getBytes());

        when(request.requestBody()).thenReturn(publishArtifactRequest.toJSON());
        doThrow(new RuntimeException("Some error")).when(dockerClient).push(eq("localhost:5000/alpine:3.6"), argumentCaptor.capture());

        final GoPluginApiResponse response = new PublishArtifactExecutor(request, consoleLogger, dockerProgressHandler, dockerClientFactory).execute();

        assertThat(response.responseCode()).isEqualTo(500);
        assertThat(response.responseBody()).contains("Failed to publish Artifact[id=id, storeId=storeId, artifactPlanConfig={\"BuildFile\":\"build.json\"}]: Some error");
    }

    @Test
    void shouldReadEnvironmentVariablesPassedFromServer() throws IOException, DockerException, InterruptedException {
        final ArtifactPlan artifactPlan = new ArtifactPlan("id", "storeId", "${IMAGE_NAME}", Optional.of("3.6"));
        final ArtifactStoreConfig storeConfig = new ArtifactStoreConfig("localhost:5000", "other", "admin", "admin123");
        final ArtifactStore artifactStore = new ArtifactStore(artifactPlan.getId(), storeConfig);
        final PublishArtifactRequest publishArtifactRequest = new PublishArtifactRequest(artifactStore, artifactPlan, agentWorkingDir.getAbsolutePath(), Collections.singletonMap("IMAGE_NAME", "alpine"));

        when(request.requestBody()).thenReturn(publishArtifactRequest.toJSON());
        when(dockerProgressHandler.getDigest()).thenReturn("foo");

        final GoPluginApiResponse response = new PublishArtifactExecutor(request, consoleLogger, dockerProgressHandler, dockerClientFactory).execute();

        verify(dockerClient).push(eq("alpine:3.6"), any(ProgressHandler.class));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"metadata\":{\"image\":\"alpine:3.6\",\"digest\":\"foo\"}}");
    }

    @Test
    @EnvironmentVariable(key = "IMAGE_NAME", value = "alpine")
    void shouldReadEnvironmentVariablesFromTheSystem() throws DockerException, InterruptedException {
        final ArtifactPlan artifactPlan = new ArtifactPlan("id", "storeId", "${IMAGE_NAME}", Optional.of("3.6"));
        final ArtifactStoreConfig storeConfig = new ArtifactStoreConfig("localhost:5000", "other", "admin", "admin123");
        final ArtifactStore artifactStore = new ArtifactStore(artifactPlan.getId(), storeConfig);
        final PublishArtifactRequest publishArtifactRequest = new PublishArtifactRequest(artifactStore, artifactPlan, agentWorkingDir.getAbsolutePath());

        when(request.requestBody()).thenReturn(publishArtifactRequest.toJSON());
        when(dockerProgressHandler.getDigest()).thenReturn("foo");

        final GoPluginApiResponse response = new PublishArtifactExecutor(request, consoleLogger, dockerProgressHandler, dockerClientFactory).execute();

        verify(dockerClient).push(eq("alpine:3.6"), any(ProgressHandler.class));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"metadata\":{\"image\":\"alpine:3.6\",\"digest\":\"foo\"}}");
    }
}
