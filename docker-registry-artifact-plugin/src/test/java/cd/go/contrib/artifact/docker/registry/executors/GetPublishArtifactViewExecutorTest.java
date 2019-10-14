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

import cd.go.contrib.artifact.docker.registry.annotation.ConfigMetadata;
import cd.go.contrib.artifact.docker.registry.annotation.MetadataHelper;
import cd.go.contrib.artifact.docker.registry.model.BuildFileArtifactPlanConfig;
import cd.go.contrib.artifact.docker.registry.model.ImageTagArtifactPlanConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cd.go.plugin.base.ResourceReader.readResource;
import static org.assertj.core.api.Assertions.assertThat;

public class GetPublishArtifactViewExecutorTest extends ViewTest {
    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = getRequestExecutor().execute();

        Map<String, String> responseHash = new Gson().fromJson(response.responseBody(), new TypeToken<Map<String,String>>(){}.getType());

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(responseHash).containsEntry("template", readResource("/docker-registry/publish-artifact.template.html"));
    }


    @Override
    protected List<ConfigMetadata> getMetadataList() {
        List<ConfigMetadata> configMetadata = new ArrayList<>();
        configMetadata.addAll(MetadataHelper.getMetadata(BuildFileArtifactPlanConfig.class));
        configMetadata.addAll(MetadataHelper.getMetadata(ImageTagArtifactPlanConfig.class));
        return configMetadata;
    }

    @Override
    protected RequestExecutor getRequestExecutor() {
        return new GetPublishArtifactViewExecutor();
    }
}
