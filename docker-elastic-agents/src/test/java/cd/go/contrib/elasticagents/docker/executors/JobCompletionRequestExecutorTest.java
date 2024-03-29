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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.MockitoAnnotations.initMocks;

class JobCompletionRequestExecutorTest {
    @Mock
    private ElasticAgentRequestClient mockPluginRequest;
    @Mock
    private DockerContainers mockAgentInstances;
    @Captor
    private ArgumentCaptor<List<Agent>> agentsArgumentCaptor;
    private JobCompletionRequestExecutor executor;
    private ClusterProfileProperties clusterProfileProperties;

    @BeforeEach
    void setUp() {
        initMocks(this);
        clusterProfileProperties = new ClusterProfileProperties();

        Map<String, DockerContainers> clusterToContainersMap = new HashMap<>();
        clusterToContainersMap.put(clusterProfileProperties.uuid(), mockAgentInstances);
        executor = new JobCompletionRequestExecutor(clusterToContainersMap, mockPluginRequest);
    }

    @Test
    void shouldTerminateElasticAgentOnJobCompletion() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier(100L);
        String elasticAgentId = "agent-1";
        JobCompletionRequest request = new JobCompletionRequest();
        request.setClusterProfileConfiguration(clusterProfileProperties)
                .setElasticAgentId(elasticAgentId)
                .setJobIdentifier(jobIdentifier)
                .setElasticProfileConfiguration(null);


        GoPluginApiResponse response = executor.execute(request);

        InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);
        inOrder.verify(mockPluginRequest).disableAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDisabled = agentsArgumentCaptor.getValue();
        assertThat(agentsToDisabled.size()).isEqualTo(1);
        assertThat(agentsToDisabled.get(0).elasticAgentId()).isEqualTo(elasticAgentId);
        inOrder.verify(mockAgentInstances).terminate(elasticAgentId, clusterProfileProperties);
        inOrder.verify(mockPluginRequest).deleteAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDelete = agentsArgumentCaptor.getValue();
        assertThat(agentsToDelete).isEqualTo(agentsToDisabled);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody().isEmpty()).isTrue();
    }
}
