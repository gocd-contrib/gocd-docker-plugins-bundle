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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.Agents;
import cd.go.contrib.elasticagents.common.exceptions.ServerRequestFailedException;
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.requests.ServerPingRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;

public class ServerPingRequestExecutor extends BaseExecutor<ServerPingRequest> {
    private final ElasticAgentRequestClient pluginRequest;

    public ServerPingRequestExecutor(Map<String, DockerServices> clusterSpecificAgentInstances,
                                     ElasticAgentRequestClient pluginRequest) {
        super(clusterSpecificAgentInstances);
        this.pluginRequest = pluginRequest;
    }

    @Override
    protected GoPluginApiResponse execute(ServerPingRequest serverPingRequest) {
        try {
            refreshInstancesForAllClusters(serverPingRequest.getAllClusterProfileConfigurations());
            LOG.info("[server-ping] Starting execute server ping request.");
            List<SwarmClusterConfiguration> allDockerSwarmClusterProfileProperties = serverPingRequest.getAllClusterProfileConfigurations();

            for (SwarmClusterConfiguration swarmClusterConfiguration : allDockerSwarmClusterProfileProperties) {
                performCleanupForACluster(swarmClusterConfiguration, clusterToServicesMap.get(swarmClusterConfiguration.uuid()));
            }

            CheckForPossiblyMissingAgents();
            return DefaultGoPluginApiResponse.success("");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void performCleanupForACluster(SwarmClusterConfiguration swarmClusterConfiguration,
                                           DockerServices dockerServices) throws Exception {
        Agents allAgents = pluginRequest.listAgents();
        Agents agentsToDisable = dockerServices.instancesCreatedAfterTimeout(swarmClusterConfiguration, allAgents);
        disableIdleAgents(agentsToDisable);

        allAgents = pluginRequest.listAgents();
        terminateDisabledAgents(allAgents, swarmClusterConfiguration, dockerServices);

        dockerServices.terminateUnregisteredInstances(swarmClusterConfiguration, allAgents);
    }

    private void terminateDisabledAgents(Agents agents,
                                         SwarmClusterConfiguration swarmClusterConfiguration,
                                         DockerServices dockerServices) throws Exception {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

        for (Agent agent : toBeDeleted) {
            dockerServices.terminate(agent.elasticAgentId(), swarmClusterConfiguration);
        }
        pluginRequest.deleteAgents(toBeDeleted);
    }

    private void CheckForPossiblyMissingAgents() {
        Collection<Agent> allAgents = pluginRequest.listAgents().agents();
        List<Agent> missingAgents = allAgents.stream().filter(agent -> clusterToServicesMap.values().stream()
                .noneMatch(instances -> instances.hasInstance(agent.elasticAgentId()))).collect(Collectors.toList());

        if (!missingAgents.isEmpty()) {
            List<String> missingAgentIds = missingAgents.stream().map(Agent::elasticAgentId).collect(Collectors.toList());
            LOG.warn("[Server Ping] Was expecting a containers with IDs " + missingAgentIds + ", but it was missing! Removing missing agents from config.");
            pluginRequest.disableAgents(missingAgents);
            pluginRequest.deleteAgents(missingAgents);
        }
    }

    private void disableIdleAgents(Agents agents) throws ServerRequestFailedException {
        pluginRequest.disableAgents(agents.findInstancesToDisable());
    }

    @Override
    protected ServerPingRequest parseRequest(String requestBody) {
        return fromJson(requestBody, ServerPingRequest.class);
    }
}
