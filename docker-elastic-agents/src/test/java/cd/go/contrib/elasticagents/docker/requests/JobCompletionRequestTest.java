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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import org.junit.jupiter.api.Test;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.assertj.core.api.Assertions.assertThat;

class JobCompletionRequestTest {
    @Test
    void shouldDeserializeFromJSON() {
        String json = "{\n" +
                "  \"elastic_agent_id\": \"ea1\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"Image\": \"alpine:latest\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"https://example.com/go\"\n" +
                "  },\n" +
                "  \"job_identifier\": {\n" +
                "    \"pipeline_name\": \"test-pipeline\",\n" +
                "    \"pipeline_counter\": 1,\n" +
                "    \"pipeline_label\": \"Test Pipeline\",\n" +
                "    \"stage_name\": \"test-stage\",\n" +
                "    \"stage_counter\": \"1\",\n" +
                "    \"job_name\": \"test-job\",\n" +
                "    \"job_id\": 100\n" +
                "  }\n" +
                "}";

        JobCompletionRequest request = fromJson(json, JobCompletionRequest.class);
        JobIdentifier expectedJobIdentifier = new JobIdentifier("test-pipeline", 1L, "Test Pipeline", "test-stage", "1", "test-job", 100L);
        JobIdentifier actualJobIdentifier = request.getJobIdentifier();
        assertThat(actualJobIdentifier).isEqualTo(expectedJobIdentifier);
        assertThat(request.getElasticAgentId()).isEqualTo("ea1");

        assertThat(request.getElasticProfileConfiguration()).isEqualTo(new ElasticProfileConfiguration().setImage("alpine:latest"));

        ClusterProfileProperties expectedClusterProfileProperties = new ClusterProfileProperties();
        expectedClusterProfileProperties.setGoServerUrl("https://example.com/go");
        assertThat(request.getClusterProfileConfiguration()).isEqualTo(expectedClusterProfileProperties);
    }
}
