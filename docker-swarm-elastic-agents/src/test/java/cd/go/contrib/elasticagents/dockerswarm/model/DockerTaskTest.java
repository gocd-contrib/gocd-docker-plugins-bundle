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

package cd.go.contrib.elasticagents.dockerswarm.model;

import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.model.reports.DockerTask;
import com.spotify.docker.client.messages.swarm.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DockerTaskTest {
    @Test
    void shouldCreateDockerContainerFromContainerObject() {
        final Task task = mock(Task.class);
        final ContainerSpec containerSpec = ContainerSpec.builder().image("gocd-agent:latest").build();
        final TaskSpec taskSpec = TaskSpec.builder().containerSpec(containerSpec).build();
        final TaskStatus taskStatus = mock(TaskStatus.class);
        final Date createdAt = new Date();
        final Service service = mock(Service.class);
        final ServiceSpec serviceSpec = ServiceSpec.builder()
                .labels(Collections.singletonMap(JOB_IDENTIFIER_LABEL_KEY, new JobIdentifier().toJson()))
                .taskTemplate(TaskSpec.builder().build())
                .build();

        when(task.id()).thenReturn("task-id");
        when(task.createdAt()).thenReturn(createdAt);
        when(task.spec()).thenReturn(taskSpec);
        when(task.nodeId()).thenReturn("node-id");
        when(task.serviceId()).thenReturn("service-id");
        when(task.status()).thenReturn(taskStatus);
        when(taskStatus.state()).thenReturn("running");
        when(service.spec()).thenReturn(serviceSpec);

        final DockerTask dockerTask = new DockerTask(task, service);

        assertThat(dockerTask.getId()).isEqualTo("task-id");
        assertThat(dockerTask.getCreated()).isEqualTo(createdAt);
        assertThat(dockerTask.getImage()).isEqualTo("gocd-agent:latest");
        assertThat(dockerTask.getServiceId()).isEqualTo("service-id");
        assertThat(dockerTask.getState()).isEqualTo("Running");
        assertThat(dockerTask.getNodeId()).isEqualTo("node-id");
    }
}
